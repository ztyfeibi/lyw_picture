package com.liyiwei.picturebase.aop;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;

import java.beans.Statement;

/**
 * type	要拦截的 MyBatis 核心接口	这里写 StatementHandler.class，表示“拦截 JDBC 语句处理阶段”。
 * method	接口里的具体方法名	"query" 表示只拦截 StatementHandler.query(...) 这个方法，而不是 update、prepare 等其他方法。
 * args	方法的参数类型列表	{Statement.class, ResultHandler.class} 精确匹配 有两个参数
 * 且类型分别为 java.sql.Statement 和 org.apache.ibatis.session.ResultHandler 的那个 query 方法，避免同名重载冲突。
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class})})
public class SQLCostInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = invocation.proceed();
        long end = System.currentTimeMillis();
        System.out.println("SQL 执行耗时：" + (end - start) + " ms");
        return result;
    }
}
