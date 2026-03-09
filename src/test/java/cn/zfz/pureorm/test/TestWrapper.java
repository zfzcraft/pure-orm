//package cn.pureorm.test;
//
//import cn.pureorm.crud.delete.DeleteSqlGenerator;
//import cn.pureorm.crud.delete.LambadaDeleteWrapper;
//import cn.pureorm.crud.select.single.LambadaSelectWrapper;
//import cn.pureorm.crud.select.single.SelectSqlGenerator;
//import cn.pureorm.crud.select.single.SelectWrapper;
//import cn.pureorm.crud.update.LambadaUpdateWrapper;
//import cn.pureorm.crud.update.UpdateSqlGenerator;
//import cn.pureorm.crud.update.UpdateWrapper;
//import cn.pureorm.crud.upsert.UpsertSqlBuilder;
//import cn.pureorm.crud.upsert.UpsertWrapper;
//import cn.pureorm.dialect.Dialect;
//import cn.pureorm.dialect.MySQLDialect;
//
//public class TestWrapper {
//    public static void main(String[] args) {
//    	Dialect dialect = new MySQLDialect();
//        // 1. SelectWrapper 示例（带括号、OR、forUpdate）
//    	LambadaSelectWrapper<User22> selectWrapper = new LambadaSelectWrapper<User22>();
//        selectWrapper .select("id", "name", "age", "status")
//                .eq("status", 1)
//                .or()
//                .beginGroup()
//                    .like("name", "张")
//                    .gt("age", 18)
//                .endGroup()
//                .conditionNative("create_time < '2026-01-01'")
//                .orderByDesc("id")
//                .limit(10)
//                .forUpdateLock();
//        System.out.println(SelectSqlGenerator.buildSql(selectWrapper, dialect));
//
//        // 2. UpdateWrapper 示例（带 setIncr、setNative）
//        LambadaUpdateWrapper<User22> updateWrapper = new LambadaUpdateWrapper<User22>();
//        updateWrapper   .set("name", "张三")
//                .setIncr("visit_count")
//                .setNative("update_time = NOW()")
//                .eq("id", 1001L)
//                .or()
//                .in("id", 1002, 1003, 1004);
//System.out.println(UpdateSqlGenerator.buildSql(updateWrapper, dialect));
//        // 3. DeleteWrapper 示例（带括号、conditionNative）
//        LambadaDeleteWrapper<User22> deleteWrapper = new LambadaDeleteWrapper<User22>();
//        deleteWrapper  .eq("status", 0)
//                .beginGroup()
//                    .lt("create_time", "2025-01-01")
//                    .or()
//                    .isNull("update_time")
//                .endGroup()
//                .conditionNative("id NOT IN (SELECT user_id FROM user_role)");
//        System.out.println(DeleteSqlGenerator.buildSql(deleteWrapper, dialect));
//        
//        UpsertWrapper<User22> upsertWrapper = new UpsertWrapper<User22>(User22.class);
//        System.out.println(UpsertSqlBuilder.buildSql(upsertWrapper, dialect));
//    }
//}
//
//



