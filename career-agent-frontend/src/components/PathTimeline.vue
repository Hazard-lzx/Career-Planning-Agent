<template>
  <!-- 垂直晋升路径时间轴 -->
  <el-timeline v-if="paths && paths.length" class="path-timeline">
    <el-timeline-item
      v-for="(item, index) in paths"
      :key="index"
      :timestamp="item.level || item.title"
      placement="top"
      :type="index === 0 ? 'primary' : 'info'"
      :hollow="index !== 0"
      size="large"
    >
      <el-card shadow="hover" class="path-card">
        <div class="path-card-inner">
          <div class="path-step-badge" v-if="item.level">
            {{ item.level }}
          </div>
          <h4>{{ item.title }}</h4>
          <p v-if="item.description" class="path-desc">{{ item.description }}</p>
          <div v-if="item.hard_skills && item.hard_skills.length" class="path-skills">
            <el-tag
              v-for="skill in item.hard_skills"
              :key="skill"
              size="small"
              effect="plain"
              class="skill-tag"
              round
            >
              {{ skill }}
            </el-tag>
          </div>
        </div>
      </el-card>
    </el-timeline-item>
  </el-timeline>
  <el-empty v-else description="暂无晋升路径数据" />
</template>

<script setup>
defineProps({
  paths: {
    type: Array,
    default: () => [],
  },
})
</script>

<style scoped>
.path-timeline {
  padding-left: 4px;
}

.path-card {
  border: 1px solid #e8f0fe;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.path-card:hover {
  border-color: #BFDBFE;
  box-shadow: 0 4px 12px rgba(59, 130, 217, 0.08);
}

.path-card-inner {
  position: relative;
}

.path-step-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 10px;
  background: linear-gradient(135deg, #EFF6FF, #DBEAFE);
  color: #3B82D9;
  font-size: 11px;
  font-weight: 600;
  margin-bottom: 8px;
}

.path-card h4 {
  color: #1e3a5f;
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 6px;
}

.path-desc {
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 10px;
}

.path-skills {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.skill-tag {
  margin: 0;
}
</style>
