package com.liyiwei.picturebase.manager.sharding;

import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.liyiwei.picturebase.model.entity.Space;
import com.liyiwei.picturebase.model.enums.SpaceLevelEnum;
import com.liyiwei.picturebase.model.enums.SpaceTypeEnum;
import com.liyiwei.picturebase.service.SpaceService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DynamicShardingManager {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private SpaceService spaceService;

    private static final String LOGIC_TABLE_NAME = "picture";
    private static final String DATABASE_NAME = "yu_picture";


    @PostConstruct
    public void init() {
        log.info("初始化动态分表配置...");
        updateShardingTableNodes();
    }

    private void updateShardingTableNodes() {
        Set<String> tableNames = fetchAllPictureTableNames();
        String newActualDataNodes = tableNames.stream()
                .map(tableName -> "yu_picture."+tableName) // 确保前缀合法
                .collect(Collectors.joining(","));
        log.info("更新分表配置，新的实际数据节点: {}", newActualDataNodes);

        ContextManager contextManager = getContextManager();
        ShardingSphereRuleMetaData ruleMetaData = contextManager.getMetaDataContexts()
                .getMetaData()
                .getDatabases()
                .get(DATABASE_NAME)
                .getRuleMetaData();
        Optional<ShardingRule> shardingRule = ruleMetaData.findSingleRule(ShardingRule.class);
        if (shardingRule.isPresent()) {
            ShardingRuleConfiguration ruleConfig = (ShardingRuleConfiguration) shardingRule.get().getConfiguration();
            List<ShardingTableRuleConfiguration> updateRules = ruleConfig.getTables()
                    .stream()
                    .map(oldTableRule -> {
                        if (LOGIC_TABLE_NAME.equals(oldTableRule.getLogicTable())){
                            ShardingTableRuleConfiguration newTableRuleConfig = new ShardingTableRuleConfiguration(LOGIC_TABLE_NAME,newActualDataNodes);
                            newTableRuleConfig.setDatabaseShardingStrategy(oldTableRule.getDatabaseShardingStrategy());
                            newTableRuleConfig.setTableShardingStrategy(oldTableRule.getTableShardingStrategy());
                            newTableRuleConfig.setKeyGenerateStrategy(oldTableRule.getKeyGenerateStrategy());
                            newTableRuleConfig.setAuditStrategy(oldTableRule.getAuditStrategy());
                        }
                        return oldTableRule;
                    }).collect(Collectors.toList());
            ruleConfig.setTables(updateRules);
            contextManager.alterRuleConfiguration(DATABASE_NAME, Collections.singleton(ruleConfig));
            contextManager.reloadDatabase(DATABASE_NAME);
            log.info("分表配置更新完成");
        }else{
            log.error("未找到分表规则，无法更新分表配置");
        }
    }

    /**
     * 动态创建分表
     */
    public void createSpacePictureTable(Space space) {
        // 动态创建分表
        // 仅为旗舰版团队空间创建分表
        if (space.getSpaceType() == SpaceTypeEnum.TEAM.getValue() && space.getSpaceLevel() == SpaceLevelEnum.FLAGSHIP.getValue()) {
            Long spaceId = space.getId();
            String tableName = "picture_" + spaceId;
            // 创建新表
            String createTableSql = "CREATE TABLE " + tableName + " LIKE picture";
            try {
                SqlRunner.db().update(createTableSql);
                // 更新分表
                updateShardingTableNodes();
            } catch (Exception e) {
                log.error("创建图片空间分表失败，空间 id = {}", space.getId());
            }
        }
    }

    /**
     * 获取所有动态表名，包括初始表picture、分表picture_{spaceId}
     */
    private Set<String> fetchAllPictureTableNames() {
        // 为了测试方便，直接对所有团队空间分表，后续只针对旗舰空间分表
        Set<Long> spaceIds = spaceService.lambdaQuery()
                .eq(Space::getSpaceType, SpaceTypeEnum.TEAM.getValue())
                .list().stream()
                .map(Space::getId)
                .collect(Collectors.toSet());
        Set<String> tableNames = spaceIds.stream()
                .map(spaceId -> LOGIC_TABLE_NAME + "_" + spaceId)
                .collect(Collectors.toSet());
        tableNames.add(LOGIC_TABLE_NAME); // 添加初始逻辑表
        return tableNames;
    }

    private ContextManager getContextManager() {
        /**
         * 向 ShardingSphere 的数据源要一个 JDBC 连接（其实是包装过的）
         * unwrap把包装的 JDBC 连接“拆包”为 ShardingSphereConnection，以便调用 ShardingSphere 扩展能力。
         */
        try (ShardingSphereConnection connection = dataSource.getConnection().unwrap(ShardingSphereConnection.class)) {
            return connection.getContextManager();
        }catch (SQLException e){
            throw new RuntimeException("获取ContextManager失败",e);
        }
    }


}
