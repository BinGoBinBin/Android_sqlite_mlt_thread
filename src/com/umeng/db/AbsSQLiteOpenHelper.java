/**
 * 
 */

package com.umeng.db;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author BinGoBinBin
 * 
 * 该类主要是使用一个计数器来跟在全局mInstance对象，避免在多线程的情况下，数据库被锁住等相关的情况。 <li>使用方法如下:
 * 
 * <pre>
 * try{
 *      SQLiteDatabase db = getDatabaseCollection();
 *      db.beginTransaction();
 *      ...
 *      db.setTransactionSuccessful();
 *  }catch( Exception e ){
 *      ...
 *  } finally {
 *      db.endTransaction();
 *      closeDatabaseCollection();
 *  }
 * </pre>
 */
public abstract class AbsSQLiteOpenHelper<T> extends SQLiteOpenHelper {

    /**
     * 全局数据库实例。在其计数为0的时候调用{@link #closeDatabaseCollection()}方法将关闭数据库连接并置null。
     */
    private static SQLiteDatabase mInstance;

    /**
     * {@link #mInstance}的计数器
     */
    private volatile AtomicInteger mReference = new AtomicInteger(0);

    /**
     * @param context
     * @param name
     * @param factory
     * @param version
     */
    public AbsSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * @param context
     * @param name
     * @param factory
     * @param version
     * @param errorHandler
     */
    public AbsSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version,
            DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    /**
     * 获取数据库连接</br>
     * 
     * @return 如果全局的SQLiteDatabase实例存在且处于open状态，则返回该实例；否则重新打开一个新的数据库连接并返回
     */
    protected synchronized SQLiteDatabase getDatabaseCollection() {
        if (mInstance == null || !mInstance.isOpen()) {
            mInstance = getWritableDatabase();
        }
        mReference.incrementAndGet();
        return mInstance;
    }

    /**
     * 当计数为0时，关闭数据库连接</br>
     */
    protected void closeDatabaseCollection() {
        mReference.decrementAndGet();
        if (mReference.get() == 0 && mInstance != null) {
            synchronized (AbsSQLiteOpenHelper.class) {
                if (mReference.get() == 0) {
                    mInstance.close();
                    mInstance = null;
                }
            }
        }
    }

    /**
     * 拼接查询条件，其查询条件都是使用AND 链接，对于需要使用OR的情况需要单独实现</br>
     * 
     * @param wheres
     * @return
     */
    protected String convertWhere(Map<String, String> wheres) {
        if (wheres == null || wheres.size() == 0) {
            return null;
        }
        String selection = null;
        // 拼接查询条件
        Set<String> keys = wheres.keySet();
        StringBuilder builder = new StringBuilder();
        final String aParams = "=?";
        final String and = " AND ";
        for (String key : keys) {
            builder.append(key).append(aParams).append(and);
        }
        int index = builder.lastIndexOf(and);
        builder.delete(index, builder.length());
        selection = builder.toString();
        return selection;
    }

    /**
     * 获取查询条件的值</br>
     * 
     * @param wheres
     * @return
     */
    protected String[] convertWhereArgs(Map<String, String> wheres) {
        if (wheres == null || wheres.size() == 0) {
            return null;
        }
        String[] selectionArgs = new String[wheres.size()];
        selectionArgs = wheres.values().toArray(selectionArgs);
        return selectionArgs;
    }

    /**
     * 判断数据是否合法，此处仅仅判空。如需要其他判断，覆盖即可</br>
     * 
     * @param items
     * @return 列表为null或者是空返回false，否则返回true
     */
    protected boolean checkData(List<T> items) {
        if (items == null || items.size() <= 0) {
            return false;
        }
        return true;
    }

    /**
     * 将items列表中的数据insert到数据库</br>
     * 
     * @param items
     */
    public void insert(List<T> items) {
        if (items == null || items.size() <= 0) {
            return;
        }
        for (T t : items) {
            insert(t);
        }
    }

    /**
     * 将t insert到数据库</br>
     * 
     * @param t
     */
    public abstract void insert(T t);

    /**
     * 更新列表中的所有数据</br>
     * 
     * @param items
     */
    public void update(List<T> items) {
        if (items == null || items.size() <= 0) {
            return;
        }
        for (T t : items) {
            update(t);
        }
    }

    /**
     * 更新某一项数据</br>
     * 
     * @param t
     */
    public abstract void update(T t);

    /**
     * 根据条件删除数据</br>
     * 
     * @param wheres
     */
    public abstract void delete(Map<String, String> wheres);

    /**
     * 根据条件查询数据</br>
     * 
     * @param wheres 条件，条件采用AND连接
     * @param orderBy 排序
     * @return 符合条件的数据
     */
    public abstract List<T> query(Map<String, String> wheres, String orderBy);

}
