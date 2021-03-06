package com.chen.f.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.f.common.pojo.SysRole;
import com.chen.f.common.pojo.SysUser;
import com.chen.f.common.pojo.enums.SysUserStatusEnum;

import java.util.List;

/**
 * <p>
 * 系统用户表 服务类
 * </p>
 *
 * @author chen
 * @since 2018-12-02
 */
public interface ISysUserService extends IService<SysUser> {
    /**
     * 获取分页的系统用户集合
     *
     * @param pageIndex         页数
     * @param pageNumber        页大小
     * @param username          用户名称
     * @param remark            用户描述
     * @param sysUserStatusEnum 用户状态
     * @param level             用户级别
     * @return 分页的系统用户集合
     */
    IPage<SysUser> getSysUserPage(Long pageIndex, Long pageNumber, String username, String remark, SysUserStatusEnum sysUserStatusEnum, Integer level);

    /**
     * 获取系统用户
     *
     * @param sysUserId 系统用户ID
     * @return 系统用户
     */
    SysUser getSysUser(String sysUserId);


    /**
     * 获取系统用户的系统角色
     *
     * @param sysUserId 系统用户ID
     * @return 系统用户的系统角色集合
     */
    List<SysRole> getSysRoleOfSysUser(String sysUserId);

    /**
     * 创建系统用户
     *
     * @param username          用户名
     * @param password          密码
     * @param remark            备注
     * @param status            状态
     * @param level             等级
     * @param operatedSysUserId 操作的系统用户ID
     */
    void createSysUser(String username, String password, String remark, SysUserStatusEnum status, Integer level, String operatedSysUserId);

    /**
     * 设置系统用户的系统角色
     *
     * @param sysUserId         设置的系统用户ID
     * @param sysRoleIdList     系统角色ID集合
     * @param operatedSysUserId 操作的系统用户
     */
    void setSysRoleOfSysUser(String sysUserId, List<String> sysRoleIdList, String operatedSysUserId);

    /**
     * 修改系统用户
     *
     * @param sysUserId         系统用户ID
     * @param username          用户名
     * @param password          密码
     * @param remark            备注
     * @param status            状态
     * @param level             等级
     * @param operatedSysUserId 操作的系统用户ID
     */
    void updateSysUser(String sysUserId, String username, String password, String remark, SysUserStatusEnum status, Integer level, String operatedSysUserId);

    /**
     * 删除系统用户
     *
     * @param sysUserId         系统用户ID
     * @param operatedSysUserId 操作的系统用户ID
     */
    void deleteSysUser(String sysUserId, String operatedSysUserId);

    /**
     * 启用系统用户
     *
     * @param sysUserId         系统用户ID
     * @param operatedSysUserId 操作的系统用户ID
     */
    void enabledSysUser(String sysUserId, String operatedSysUserId);

    /**
     * 禁用系统用户
     *
     * @param sysUserId         系统用户ID
     * @param operatedSysUserId 操作的系统用户ID
     */
    void disableSysUser(String sysUserId, String operatedSysUserId);

    /**
     * 锁定系统用户
     *
     * @param sysUserId         系统用户ID
     * @param operatedSysUserId 操作的系统用户ID
     */
    void lockSysUser(String sysUserId, String operatedSysUserId);

    /**
     * 过期系统用户
     *
     * @param sysUserId         系统用户ID
     * @param operatedSysUserId 操作的系统用户ID
     */
    void expireSysUser(String sysUserId, String operatedSysUserId);

}
