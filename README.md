Android_sqlite_mult_thread
=========================
在多线程的时候，我们需要不断的打开、关闭数据库，同时很容易出现数据库被锁住、操作一个已经被关闭的数据库等，简单的继承该类即可解决多线程的问题。    
使用方式如下：    
1:继承AbsSQLiteOpenHelper类。根据自己的逻辑实现增删改查操作       
2：以insert方法为例，在insert方法中复制以下代码并实现自己的insert逻辑       
```java
 try{
      SQLiteDatabase db = getDatabaseCollection();
      db.beginTransaction();
      // do something
      db.setTransactionSuccessful();
  }catch( Exception e ){
      // do something
  } finally {
      db.endTransaction();
      closeDatabaseCollection();
  }
