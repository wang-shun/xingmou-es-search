package com.gi.xm.es.dbutil;
  
//数据库连接池  单例模式  
  
import java.sql.Connection;  
import java.sql.SQLException;  
import com.mchange.v2.c3p0.ComboPooledDataSource;  
import com.mchange.v2.c3p0.DataSources;  
  
  
public final class ConnectionManager {  
  
    private static ConnectionManager instance;  
  
    private ComboPooledDataSource ds;  
  
    private ConnectionManager() throws Exception {  

        ds = new ComboPooledDataSource();
        ds.setDriverClass("com.mysql.cj.jdbc.Driver");
        ds.setJdbcUrl("jdbc:mysql://10.9.130.142/app?characterEncoding=UTF-8&useOldAliasMetadataBehavior=true");
        ds.setUser("root");
        ds.setPassword("IhNtPz6E2V34");
  
        //初始化时获取三个连接，取值应在minPoolSize与maxPoolSize之间。Default: 3 initialPoolSize  
        ds.setInitialPoolSize(10);
        //连接池中保留的最大连接数。Default:  15 maxPoolSize  
        ds.setMaxPoolSize(20);
          
        //// 连接池中保留的最小连接数。  
        ds.setMinPoolSize(5);
          
        //当连接池中的连接耗尽的时候c3p0一次同时获取的连接数。Default: 3 acquireIncrement    
        //ds.setAcquireIncrement(1);
  
        //每60秒检查所有连接池中的空闲连接。Default: 0  idleConnectionTestPeriod  
        //ds.setIdleConnectionTestPeriod(60);
         
        //最大空闲时间,25000秒内未使用则连接被丢弃。若为0则永不丢弃。Default: 0  maxIdleTime  
        //ds.setMaxIdleTime(25000);
        //连接关闭时默认将所有未提交的操作回滚。Default: false autoCommitOnClose  
        //ds.setAutoCommitOnClose(true);
  
        //定义所有连接测试都执行的测试语句。在使用连接测试的情况下这个一显著提高测试速度。注意：  
        //测试的表必须在初始数据源的时候就存在。Default: null  preferredTestQuery  
       // ds.setPreferredTestQuery("select sysdate from dual");
        // 因性能消耗大请只在需要的时候使用它。如果设为true那么在每个connection提交的  
        // 时候都将校验其有效性。建议使用idleConnectionTestPeriod或automaticTestTable  
        // 等方法来提升连接测试的性能。Default: false testConnectionOnCheckout  
        //ds.setTestConnectionOnCheckout(true);
        //如果设为true那么在取得连接的同时将校验连接的有效性。Default: false  testConnectionOnCheckin  
        //ds.setTestConnectionOnCheckin(true);
  
        //定义在从数据库获取新连接失败后重复尝试的次数。Default: 30  acquireRetryAttempts  
        ds.setAcquireRetryAttempts(30);  
        //两次连接中间隔时间，单位毫秒。Default: 1000 acquireRetryDelay  
        //ds.setAcquireRetryDelay(1000);
        //获取连接失败将会引起所有等待连接池来获取连接的线程抛出异常。但是数据源仍有效  
        //保留，并在下次调用getConnection()的时候继续尝试获取连接。如果设为true，那么在尝试  
        //获取连接失败后该数据源将申明已断开并永久关闭。Default: false  breakAfterAcquireFailure  
        ds.setBreakAfterAcquireFailure(true);  
         
         
  
        //        <!--当连接池用完时客户端调用getConnection()后等待获取新连接的时间，超时后将抛出  
        //        SQLException,如设为0则无限期等待。单位毫秒。Default: 0 -->  
        //        <property name="checkoutTimeout">100</property>  
  
        //        <!--c3p0将建一张名为Test的空表，并使用其自带的查询语句进行测试。如果定义了这个参数那么  
        //        属性preferredTestQuery将被忽略。你不能在这张Test表上进行任何操作，它将只供c3p0测试  
        //        使用。Default: null-->  
        //        <property name="automaticTestTable">Test</property>  
  
        //        <!--JDBC的标准参数，用以控制数据源内加载的PreparedStatements数量。但由于预缓存的statements  
        //        属于单个connection而不是整个连接池。所以设置这个参数需要考虑到多方面的因素。  
        //        如果maxStatements与maxStatementsPerConnection均为0，则缓存被关闭。Default: 0-->  
        //        <property name="maxStatements">100</property>  
  
        //        <!--maxStatementsPerConnection定义了连接池内单个连接所拥有的最大缓存statements数。Default: 0 -->  
        //        <property name="maxStatementsPerConnection"></property>  
  
        //        <!--c3p0是异步操作的，缓慢的JDBC操作通过帮助进程完成。扩展这些操作可以有效的提升性能  
        //        通过多线程实现多个操作同时被执行。Default: 3-->  
        //        <property name="numHelperThreads">3</property>  
  
        //        <!--用户修改系统配置参数执行前最多等待300秒。Default: 300 -->  
        //        <property name="propertyCycle">300</property>  
  
    }  
  
    public  static  final ConnectionManager getInstance() {  
        if (instance == null) {  
            try {  
                instance = new ConnectionManager();  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
        return instance;  
    }  
  
    public synchronized   final Connection getConnection() {  
        try {
            return ds.getConnection();
        } catch (SQLException e) {  
            e.printStackTrace();  
        }  
        return null;  
    }  
  
    protected void finalize() throws Throwable {  
        DataSources.destroy(ds); //关闭datasource  
        super.finalize();  
    }  
  
}  