package com.career.agent.service.impl;

import com.career.agent.ai.AIService;
import com.career.agent.config.AiConfig;
import com.career.agent.dto.MatchResultDTO;
import com.career.agent.entity.Job;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 核心 AI 服务实现 (LangChain4j 版本)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final ChatLanguageModel chatLanguageModel;
    private final ObjectMapper objectMapper;
    private final Driver neo4jDriver;

    private static final Pattern MD_JSON_PAT =
            Pattern.compile("```(?:json)?\\s*\\n?(.*?)\\n?\\s*```", Pattern.DOTALL);

    // ==================== 1. 简历解析 ====================
    @Override
    public StudentProfile parseResume(String resumeText) {
        String template = AiConfig.loadPromptTemplate("parse_resume.txt");
        String prompt = template.replace("{resume_text}", resumeText);
        String raw = callModel(prompt, "简历解析");
        return parseJson(raw, StudentProfile.class, "简历解析");
    }

    // ==================== 2. 岗位解析 ====================
    @Override
    public JobProfile parseJob(String jobDescription, String title) {
        String template = AiConfig.loadPromptTemplate("parse_job.txt");
        String jobTitle = (title != null && !title.isEmpty()) ? title : "未知岗位";
        String prompt = template.replace("{title}", jobTitle)
                .replace("{job_description}", jobDescription);
        String raw = callModel(prompt, "岗位解析");
        return parseJson(raw, JobProfile.class, "岗位解析");
    }

    // ==================== 3. 人岗匹配 (1 次 LLM 组合调用 + 20s 超时兜底) ====================
    @Override
    public MatchResultDTO match(StudentProfile sp, JobProfile jp, Map<String, Double> weights) {
        if (weights == null) { weights = new LinkedHashMap<>(); weights.put("basic",0.2); weights.put("hard_skill",0.4); weights.put("soft_skill",0.25); weights.put("potential",0.15); }
        if (sp.getEducation() == null) sp.setEducation(new Education());
        if (jp.getBasicRequirements() == null) jp.setBasicRequirements(new BasicRequirements());
        if (sp.getSkills() == null) sp.setSkills(List.of());
        if (jp.getHardSkills() == null) jp.setHardSkills(List.of());
        if (sp.getSoftSkills() == null) sp.setSoftSkills(Map.of());
        if (jp.getSoftSkills() == null) jp.setSoftSkills(Map.of());

        double basicScore = calcBasicScore(sp, jp);
        HardSkillResult hardResult = calcHardSkillScore(sp, jp);

        // 1 次组合 LLM 调用（软技能 + 潜力 + 差距分析），20s 超时，失败则降级规则计算
        double softScore, potentialScore;
        String gap;
        try {
            String tpl = AiConfig.loadPromptTemplate("match_combined.txt");
            String prompt = tpl
                    .replace("{student_profile}", toJson(sp))
                    .replace("{job_profile}", toJson(jp))
                    .replace("{hard_skill_score}", String.valueOf(round2(hardResult.score)))
                    .replace("{basic_score}", String.valueOf(round2(basicScore)));
            Future<String> future = Executors.newSingleThreadExecutor().submit(() -> callModel(prompt, "组合分析"));
            String raw = future.get(20, TimeUnit.SECONDS);
            Map<String, Object> combined = parseJsonToMap(raw, "组合分析");
            softScore = clamp(parseDouble(combined.getOrDefault("soft_skill_score","0.5").toString()), 0, 1);
            potentialScore = clamp(parseDouble(combined.getOrDefault("potential_score","0.5").toString()), 0, 1);
            gap = Objects.toString(combined.get("gap_analysis"), "");
            if (gap.isEmpty()) throw new RuntimeException("gap_analysis empty");
        } catch (Exception e) {
            log.warn("组合 LLM 失败/超时, 降级规则计算: {}", e.getMessage());
            softScore = calcSoftSkillByRule(sp.getSoftSkills(), jp.getSoftSkills());
            potentialScore = calcPotentialByRule(sp);
            gap = String.format("基础条件匹配度 %.0f%%, 硬技能匹配度 %.0f%%. 已具备: %s. 需补充: %s.",
                    basicScore * 100, hardResult.score * 100, hardResult.matched, hardResult.missing);
        }

        double total = round2(basicScore * weights.getOrDefault("basic",0.2) +
                hardResult.score * weights.getOrDefault("hard_skill",0.4) +
                softScore * weights.getOrDefault("soft_skill",0.25) +
                potentialScore * weights.getOrDefault("potential",0.15));

        MatchResultDTO r = new MatchResultDTO(); r.setTotalScore(total);
        MatchResultDTO.Dimensions dim = new MatchResultDTO.Dimensions();
        dim.setBasic(round2(basicScore)); dim.setHardSkill(round2(hardResult.score));
        dim.setSoftSkill(round2(softScore)); dim.setPotential(round2(potentialScore));
        r.setDimensions(dim); r.setGapAnalysis(gap);
        r.setMatchedSkills(hardResult.matched); r.setMissingSkills(hardResult.missing);
        log.info("匹配完成, 总分={}", total);
        return r;
    }

    private double calcBasicScore(StudentProfile sp, JobProfile jp) {
        double score = 0; int count = 0;
        Map<String,Integer> dm = Map.of("专科",1,"大专",1,"本科",2,"学士",2,"硕士",3,"研究生",3,"博士",4);
        String jd = jp.getBasicRequirements().getDegree();
        if (jd != null && !jd.isEmpty()) { count++;
            int sl = dm.getOrDefault(sp.getEducation().getDegree()!=null?sp.getEducation().getDegree():"", 0);
            int jl = dm.getOrDefault(jd, 0);
            if (sl >= jl) score += 1; else if (sl == jl - 1) score += 0.5; }
        String jm = jp.getBasicRequirements().getMajor();
        if (jm != null && !jm.isEmpty()) { count++;
            String sm = sp.getEducation().getMajor() != null ? sp.getEducation().getMajor() : "";
            if (jm.equals(sm)) score += 1; else if (isMajorRelated(sm, jm)) score += 0.5; }
        return count > 0 ? score / count : 0.5;
    }

    private boolean isMajorRelated(String sm, String jm) {
        String sl = sm.toLowerCase(), jl = jm.toLowerCase();
        if (sl.contains(jl) || jl.contains(sl)) return true;
        Map<String,List<String>> g = Map.of("计算机",List.of("计算机","软件","信息","网络","数据","人工智能","AI"),"电子",List.of("电子","通信","自动化","电气"),"金融",List.of("金融","经济","会计","财务"),"管理",List.of("管理","工商","市场","人力"),"机械",List.of("机械","制造","材料"));
        for (List<String> k : g.values()) if (k.stream().anyMatch(sl::contains) && k.stream().anyMatch(jl::contains)) return true;
        return false;
    }

    private HardSkillResult calcHardSkillScore(StudentProfile sp, JobProfile jp) {
        Set<String> ss = sp.getSkills().stream().map(String::toLowerCase).collect(Collectors.toSet());
        List<String> jr = jp.getHardSkills();
        Set<String> js = jr.stream().map(String::toLowerCase).collect(Collectors.toSet());
        Set<String> matched = new HashSet<>(ss); matched.retainAll(js);
        Set<String> missing = new HashSet<>(js); missing.removeAll(ss);
        Set<String> union = new HashSet<>(ss); union.addAll(js);
        double score = union.isEmpty() ? 0 : (double)matched.size()/union.size();
        List<String> ml = jr.stream().filter(s->matched.contains(s.toLowerCase())).collect(Collectors.toList());
        List<String> xl = jr.stream().filter(s->missing.contains(s.toLowerCase())).collect(Collectors.toList());
        return new HardSkillResult(score, ml, xl);
    }

    private double calcSoftSkillByRule(Map<String,String> ss, Map<String,String> js) {
        Map<String,Integer> lm = Map.of("强",3,"高",3,"中",2,"低",1,"弱",1);
        double td = 0; int n = 0;
        for (Map.Entry<String,String> e : js.entrySet()) {
            int rv = lm.getOrDefault(e.getValue(), 2);
            int sv = lm.getOrDefault(ss.getOrDefault(e.getKey(),"弱"), 1);
            td += Math.abs(rv - sv); n++; }
        return n == 0 ? 0.5 : round2(Math.max(0, 1 - td / n / 2.0));
    }

    private double calcPotentialByRule(StudentProfile sp) {
        double cs = Math.min(sp.getCertificates().size() * 0.1, 0.3);
        double is = Math.min(sp.getInternships().size() * 0.15, 0.35);
        double ps = Math.min(sp.getProjects().size() * 0.1, 0.35);
        return round2(Math.min(cs + is + ps, 1));
    }

    // ==================== 4. 文本润色 ====================
    @Override
    public String polish(String rawText, String section) {
        String template = AiConfig.loadPromptTemplate("polish.txt");
        String label = (section != null && !section.isEmpty()) ? section : "职业规划报告";
        String prompt = template.replace("{section}", label).replace("{raw_text}", rawText);
        try { return callModel(prompt, "文本润色"); }
        catch (Exception e) { log.warn("润色失败, 返回原文", e); return rawText; }
    }

    // ==================== 5. 图谱构建 ====================
    @Override
    public void buildJobGraph(List<Job> jobs) {
        if (jobs == null || jobs.isEmpty()) return;
        ObjectMapper m = new ObjectMapper();
        List<ParsedJob> parsed = new ArrayList<>();
        for (Job j : jobs) {
            ParsedJob p = new ParsedJob(); p.id=j.getId(); p.jobCode=j.getJobCode(); p.title=j.getTitle(); p.level=j.getLevel();
            p.keyword=extractKeyword(j.getTitle()); p.levelOrder=levelOrder(j.getLevel()); p.profileJson=j.getProfileJson()!=null?j.getProfileJson():"{}";
            if (j.getProfileJson() != null) try { @SuppressWarnings("unchecked") Map<String,Object> pf = m.readValue(j.getProfileJson(),new TypeReference<>(){});
                @SuppressWarnings("unchecked") List<String> sk = (List<String>)pf.getOrDefault("hard_skills",new ArrayList<>()); p.hardSkills=sk; } catch (Exception ignored) {}
            parsed.add(p);
        }
        int nodes=0,rels=0;
        try (Session s = neo4jDriver.session()) {
            s.run("MATCH (n:Job) DETACH DELETE n");
            for (ParsedJob p : parsed) { s.run("CREATE (j:Job {jobId:$id,jobCode:$jc,title:$t,level:$l,profileJson:$pj})",Map.of("id",p.id,"jc",nv(p.jobCode),"t",nv(p.title),"l",nv(p.level),"pj",nv(p.profileJson))); nodes++; }
            Map<String,List<ParsedJob>> groups = new LinkedHashMap<>();
            for (ParsedJob p : parsed) groups.computeIfAbsent(p.keyword,k->new ArrayList<>()).add(p);
            for (List<ParsedJob> g : groups.values()) { g.sort(Comparator.comparingInt(x->x.levelOrder)); for (int i=0;i<g.size()-1;i++) { s.run("MATCH (a:Job {jobId:$a}) MATCH (b:Job {jobId:$b}) CREATE (a)-[:PROMOTE_TO]->(b)",Map.of("a",g.get(i).id,"b",g.get(i+1).id)); rels++; } }
            for (int i=0;i<parsed.size();i++) for (int j=i+1;j<parsed.size();j++) { ParsedJob a=parsed.get(i),b=parsed.get(j); if (!a.keyword.isEmpty()&&a.keyword.equals(b.keyword)) continue; double sim=jaccard(a.hardSkills,b.hardSkills); if (sim>0.6) { String reason=genSwitchReason(a,b,sim); s.run("MATCH (a:Job {jobId:$a}) MATCH (b:Job {jobId:$b}) CREATE (a)-[:CAN_SWITCH_TO {similarity:$s,reason:$r}]->(b)",Map.of("a",a.id,"b",b.id,"s",round2(sim),"r",reason)); rels++; } }
            log.info("图谱完成: 节点{}, 关系{}", nodes, rels);
        }
    }
    private String genSwitchReason(ParsedJob a, ParsedJob b, double sim) { String t=AiConfig.loadPromptTemplate("graph_switch_reason.txt"); String p=t.replace("{job_a_title}",nv(a.title)).replace("{job_a_level}",nv(a.level)).replace("{job_a_skills}",String.join(", ",a.hardSkills)).replace("{job_b_title}",nv(b.title)).replace("{job_b_level}",nv(b.level)).replace("{job_b_skills}",String.join(", ",b.hardSkills)).replace("{similarity}",String.format("%.2f",sim)); try { return callModel(p,"换岗原因"); } catch(Exception e) { return String.format("技能相似度%.0f%%, 具备转型基础",sim*100); } }

    // ==================== 工具方法 ====================
    private String callModel(String prompt, String operation) {
        try { String resp = chatLanguageModel.generate(prompt); if (resp == null || resp.isBlank()) throw new RuntimeException(operation+": 模型返回空响应"); return resp; }
        catch (RuntimeException e) { throw e; } catch (Exception e) { throw new RuntimeException(operation+" 大模型调用失败: "+e.getMessage(), e); }
    }
    private <T> T parseJson(String raw, Class<T> clazz, String op) {
        String text = raw; Matcher m = MD_JSON_PAT.matcher(text); if (m.find()) text = m.group(1).trim();
        try { return objectMapper.readValue(text, clazz); } catch (JsonProcessingException e) {
            int start = text.indexOf('{'); if (start >= 0) { int depth=0,end=-1; for(int i=start;i<text.length();i++) { char c=text.charAt(i); if(c=='{')depth++;else if(c=='}'){depth--;if(depth==0){end=i+1;break;}} } if(end>start) { try { return objectMapper.readValue(text.substring(start,end),clazz); } catch(JsonProcessingException ignored){} } }
            throw new RuntimeException(op+" JSON 解析失败, 原始响应前200字符: "+raw.substring(0,Math.min(200,raw.length()))); }
    }
    @SuppressWarnings("unchecked")
    private Map<String,Object> parseJsonToMap(String raw, String op) {
        String text = raw; Matcher m = MD_JSON_PAT.matcher(text); if (m.find()) text = m.group(1).trim();
        try { return objectMapper.readValue(text, new TypeReference<Map<String,Object>>(){}); } catch(JsonProcessingException e) {
            int start = text.indexOf('{'); if(start>=0){int depth=0,end=-1;for(int i=start;i<text.length();i++){char c=text.charAt(i);if(c=='{')depth++;else if(c=='}'){depth--;if(depth==0){end=i+1;break;}}}if(end>start){try{return objectMapper.readValue(text.substring(start,end),new TypeReference<Map<String,Object>>(){});}catch(JsonProcessingException ignored){}}}
            throw new RuntimeException(op+" JSON 解析失败"); }
    }
    private String toJson(Object obj) { try { return objectMapper.writeValueAsString(obj); } catch(Exception e) { return "{}"; } }
    private double parseDouble(String s) { return Double.parseDouble(s.replaceAll("[^0-9.]","").trim()); }
    private static double clamp(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }
    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
    private String extractKeyword(String t) { if(t==null)return""; for(String w:new String[]{"初级","中级","高级","资深","专家","实习","Junior","Senior","工程师","开发","岗位","职位"}) t=t.replace(w,""); return t.trim(); }
    private int levelOrder(String l) { if(l==null)return 2; switch(l) { case "实习":case "实习生": return 0; case "初级":case "初级工程师":case "Junior":case "junior": return 1; case "中级":case "中级工程师":case "Mid":case "mid": return 2; case "高级":case "高级工程师":case "Senior":case "senior": return 3; case "资深":case "资深工程师":case "主任": return 4; case "专家":case "架构师":case "总监": return 5; case "VP": return 6; default: return 2; } }
    private double jaccard(List<String> a, List<String> b) { Set<String> sa=a.stream().map(String::toLowerCase).collect(Collectors.toSet()); Set<String> sb=b.stream().map(String::toLowerCase).collect(Collectors.toSet()); Set<String> inter=new HashSet<>(sa); inter.retainAll(sb); Set<String> union=new HashSet<>(sa); union.addAll(sb); return union.isEmpty()?0:(double)inter.size()/union.size(); }
    private static String nv(String s) { return s==null?"":s; }

    private static class HardSkillResult { final double score; final List<String> matched,missing; HardSkillResult(double s,List<String> m,List<String> x){score=s;matched=m;missing=x;} }
    private static class ParsedJob { Long id; String jobCode,title,level,keyword=""; int levelOrder; String profileJson="{}"; List<String> hardSkills=new ArrayList<>(); }
}
