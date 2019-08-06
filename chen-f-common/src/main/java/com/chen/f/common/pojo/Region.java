package com.chen.f.common.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 地区表
 * </p>
 *
 * @author chen
 * @since 2018-12-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("REGION")
@ApiModel(value="Region对象", description="地区表")
public class Region extends Model<Region> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private Integer id;

    @ApiModelProperty(value = "父级id")
    @TableField("PARENT_ID")
    private Integer parentId;

    @ApiModelProperty(value = "地区等级(0:国家;1:省;2:市;3:区/县;)")
    @TableField("LEVEL")
    private Integer level;

    @ApiModelProperty(value = "名称")
    @TableField("NAME")
    private String name;

    @ApiModelProperty(value = "城市编码")
    @TableField("CITY_CODE")
    private String cityCode;

    @ApiModelProperty(value = "邮政编码")
    @TableField("POSTAL_CODE")
    private String postalCode;

    @ApiModelProperty(value = "简称")
    @TableField("SHORT_NAME")
    private String shortName;

    @ApiModelProperty(value = "全称")
    @TableField("FULL_NAME")
    private String fullName;

    @ApiModelProperty(value = "英文名称")
    @TableField("ENGLISH_NAME")
    private String englishName;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}