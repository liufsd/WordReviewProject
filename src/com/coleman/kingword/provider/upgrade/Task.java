
package com.coleman.kingword.provider.upgrade;

/**
 * 执行数据库升级任务
 * 
 * @author coleman
 * @version [version, Jun 21, 2012]
 * @see [relevant class/method]
 * @since [product/module version]
 */
public interface Task {
    /**
     * 执行数据库升级:
     * <p>
     * 1. 读取需要读取的数据
     * <p>
     * 2. 删除无用的表结构
     * <p>
     * 3. 创建新的表结构
     * <p>
     * 4. 写入之前读取的数据
     * <p>
     * 5. 保存升级后的数据库版本（也就是软件版本号）
     */
    public void execute();
}
