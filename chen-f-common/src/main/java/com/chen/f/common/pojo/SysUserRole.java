package com.chen.f.common.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 系统用户角色表
 * </p>
 *
 * @author chen
 * @since 2019-02-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("SYS_USER_ROLE")
@ApiModel(value="SysUserRole对象", description="系统用户角色表")
public class SysUserRole extends Model<SysUserRole> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "系统用户id")
    @TableId(value = "SYS_USER_ID", type = IdType.ID_WORKER_STR)
    private String sysUserId;

    @ApiModelProperty(value = "系统角色id")
    @TableField("SYS_ROLE_ID")
    private String sysRoleId;

    @ApiModelProperty(value = "创建系统用户id('为初始化创建)")
    @TableField("CREATE_SYS_USER_ID")
    private String createSysUserId;

    @TableField("CREATE_DATE_TIME")
    private LocalDateTime createDateTime;


    @Override
    protected Serializable pkVal() {
        return this.sysUserId;
    }

}