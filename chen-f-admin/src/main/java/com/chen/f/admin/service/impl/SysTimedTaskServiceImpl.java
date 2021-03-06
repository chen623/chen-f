package com.chen.f.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.f.admin.configuration.helper.QuartzHelper;
import com.chen.f.admin.service.ISysTimedTaskService;
import com.chen.f.common.mapper.SysTimedTaskMapper;
import com.chen.f.common.pojo.SysTimedTask;
import com.chen.f.common.pojo.enums.StatusEnum;
import com.chen.f.common.pojo.enums.SysTimedTaskTypeEnum;
import com.chen.f.common.api.ApiAssert;
import com.chen.f.common.api.exception.ApiException;
import com.chen.f.common.api.response.error.ErrorResponse;
import com.chen.f.core.util.JacksonUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 系统定时任务表 服务实现类
 * </p>
 *
 * @author chen
 * @since 2018-11-10
 */
@Service
public class SysTimedTaskServiceImpl extends ServiceImpl<SysTimedTaskMapper, SysTimedTask> implements ISysTimedTaskService {
    protected static final Logger logger = LoggerFactory.getLogger(SysTimedTaskServiceImpl.class);

    
    @Autowired
    private SysTimedTaskMapper sysTimedTaskMapper;

    @Override
    public IPage<SysTimedTask> getSysTimedTaskPage(Long pageIndex, Long pageNumber,
                                                   String code, String name, String className, SysTimedTaskTypeEnum type, String remark, StatusEnum status) {
        LambdaQueryWrapper<SysTimedTask> queryWrapper = Wrappers.<SysTimedTask>lambdaQuery();
        if (StringUtils.isNotBlank(code)) {
            queryWrapper.eq(SysTimedTask::getCode, code);
        }
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.eq(SysTimedTask::getName, name);
        }
        if (StringUtils.isNotBlank(className)) {
            queryWrapper.eq(SysTimedTask::getClassName, className);
        }
        if (Objects.nonNull(type)) {
            queryWrapper.eq(SysTimedTask::getType, type);
        }
        if (StringUtils.isNotBlank(remark)) {
            queryWrapper.like(SysTimedTask::getRemark, remark);
        }
        if (Objects.nonNull(status)) {
            queryWrapper.eq(SysTimedTask::getStatus, status);
        }
        return sysTimedTaskMapper.selectPage(new Page<>(pageIndex, pageNumber), queryWrapper);
    }

    @Override
    public List<SysTimedTask> getEnabledSysTimedTaskList() {
        return sysTimedTaskMapper.selectList(Wrappers.<SysTimedTask>lambdaQuery().eq(SysTimedTask::getStatus, StatusEnum.ENABLED));
    }

    @Override
    public SysTimedTask getSysTimedTask(String sysTimedTaskId) {
        ApiAssert.isNotBlank(sysTimedTaskId, ErrorResponse.create("系统定时任务ID不能为空"));
        return sysTimedTaskMapper.selectById(sysTimedTaskId);
    }

    @Override
    public SysTimedTask getSysTimedTaskByCode(String code) {
        ApiAssert.isNotBlank(code, ErrorResponse.create("系统定时任务标识不能为空"));
        return sysTimedTaskMapper.selectOne(Wrappers.<SysTimedTask>lambdaQuery().eq(SysTimedTask::getCode, code));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    @SuppressWarnings("unchecked")
    public void createSysTimedTask(String code, String name, String jobClassName, String cronExpression, String data, SysTimedTaskTypeEnum sysTimedTaskTypeEnum, String remark, StatusEnum statusEnum, String operatedSysUserId) {
        ApiAssert.isNotBlank(code, ErrorResponse.create("系统定时任务标识不能为空"));
        ApiAssert.isNotBlank(name, ErrorResponse.create("系统定时任务名称不能为空"));
        ApiAssert.isNotBlank(jobClassName, ErrorResponse.create("系统定时任务任务类名称不能为空"));
        ApiAssert.isNotBlank(cronExpression, ErrorResponse.create("系统定时任务cron表达式不能为空"));
        ApiAssert.isNotNull(sysTimedTaskTypeEnum, ErrorResponse.create("系统定时任务类型不能为空"));
        ApiAssert.isNotNull(statusEnum, ErrorResponse.create("系统定时任务状态不能为空"));
        ApiAssert.isNotBlank(operatedSysUserId, ErrorResponse.create("操作的系统用户ID不能为空"));
        Class<?> jobClass;
        try {
            jobClass = Class.forName(jobClassName);
        } catch (ClassNotFoundException e) {
            logger.warn("没有找到系统定时任务Class");
            throw new ApiException(ErrorResponse.create(String.format("没有找到系统定时任务类%s", jobClassName)), e);
        }
        logger.debug("检查jobClass是否是Job.class的子类");
        ApiAssert.isAssignable(Job.class, jobClass, ErrorResponse.create(String.format("系统定时任务%s没有继承Job类", jobClass)));
        Map dataMap = null;
        if (StringUtils.isNotBlank(data)) {
            dataMap = JacksonUtils.parseObject(data, Map.class);
        }
        if (statusEnum == StatusEnum.ENABLED) {
            logger.debug("添加系统定时任务");
            try {
                QuartzHelper.addJob(code, code, code, code, (Class<? extends Job>) jobClass, cronExpression, dataMap);
            } catch (SchedulerException e) {
                logger.warn("添加系统定时任务失败", e);
                throw new ApiException(ErrorResponse.create("添加定时任务失败"), e);
            }
        }
        logger.debug("插入系统定时任务到数据库");
        SysTimedTask sysTimedTask = new SysTimedTask();
        sysTimedTask.setCode(code);
        sysTimedTask.setName(name);
        sysTimedTask.setClassName(jobClassName);
        sysTimedTask.setCronExpression(cronExpression);
        sysTimedTask.setData(data);
        sysTimedTask.setType(sysTimedTaskTypeEnum);
        sysTimedTask.setStatus(statusEnum);
        sysTimedTask.setRemark(remark);
        sysTimedTask.setCreateSysUserId(operatedSysUserId);
        sysTimedTask.setCreateDateTime(LocalDateTime.now());
        sysTimedTaskMapper.insert(sysTimedTask);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    @SuppressWarnings("unchecked")
    public void updateSysTimedTask(String sysTimedTaskId, String code, String name, String jobClassName, String cronExpression, String data, SysTimedTaskTypeEnum sysTimedTaskTypeEnum, String remark, StatusEnum statusEnum, String operatedSysUserId) {
        ApiAssert.isNotBlank(sysTimedTaskId, ErrorResponse.create("系统定时任务ID不能为空"));
        ApiAssert.isNotBlank(code, ErrorResponse.create("系统定时任务标识不能为空"));
        ApiAssert.isNotBlank(name, ErrorResponse.create("系统定时任务名称不能为空"));
        ApiAssert.isNotBlank(jobClassName, ErrorResponse.create("系统定时任务任务类名称不能为空"));
        ApiAssert.isNotBlank(cronExpression, ErrorResponse.create("系统定时任务CRON表达式不能为空"));
        ApiAssert.isNotNull(sysTimedTaskTypeEnum, ErrorResponse.create("系统定时任务类型不能为空"));
        ApiAssert.isNotNull(statusEnum, ErrorResponse.create("系统定时任务状态不能为空"));
        ApiAssert.isNotBlank(operatedSysUserId, ErrorResponse.create("操作的系统用户ID不能为空"));

        logger.debug("获取系统定时任务信息");
        SysTimedTask sysTimedTask = sysTimedTaskMapper.selectById(sysTimedTaskId);
        ApiAssert.isNotNull(sysTimedTask, ErrorResponse.create("系统定时任务不存在"));
        Class<?> jobClass;
        try {
            jobClass = Class.forName(jobClassName);
        } catch (ClassNotFoundException e) {
            logger.warn("没有找到系统定时任务class");
            throw new ApiException(ErrorResponse.create(String.format("没有找到系统定时任务类%s", jobClassName)), e);
        }
        ApiAssert.isAssignable(Job.class, jobClass, ErrorResponse.create(String.format("系统定时任务%s没有继承Job类", jobClass)));
        Map dataMap = null;
        if (StringUtils.isNotBlank(data)) {
            dataMap = JacksonUtils.parseObject(data, Map.class);
        }
        logger.debug("修改系统定时任务");
        try {
            QuartzHelper.updateJob(code, code, code, code, (Class<? extends Job>) jobClass, cronExpression, dataMap);
        } catch (SchedulerException e) {
            logger.warn("添加系统定时任务失败", e);
            throw new ApiException(ErrorResponse.create("添加系统定时任务失败"), e);
        }
        if (statusEnum == StatusEnum.DISABLE) {
            this.disableSysTimedTaskByCode(code, operatedSysUserId);
        }
        logger.debug("修改系统定时任务数据库记录");
        sysTimedTask.setCode(code);
        sysTimedTask.setName(name);
        sysTimedTask.setClassName(jobClassName);
        sysTimedTask.setCronExpression(cronExpression);
        sysTimedTask.setData(data);
        sysTimedTask.setType(sysTimedTaskTypeEnum);
        sysTimedTask.setStatus(statusEnum);
        sysTimedTask.setRemark(remark);
        sysTimedTask.setUpdateSysUserId(operatedSysUserId);
        sysTimedTask.setUpdateDateTime(LocalDateTime.now());
        sysTimedTaskMapper.updateById(sysTimedTask);
    }

    @Override
    public void deleteSysTimedTask(String sysTimedTaskId) {
        ApiAssert.isNotBlank(sysTimedTaskId, ErrorResponse.create("系统定时任务ID不能为空"));
        logger.debug("检查系统定时任务是否存在");
        SysTimedTask sysTimedTask = sysTimedTaskMapper.selectById(sysTimedTaskId);
        ApiAssert.isNotNull(sysTimedTask, ErrorResponse.create("系统系统定时任务不存在"));
        logger.debug("删除系统定时任务");
        try {
            QuartzHelper.deleteJob(sysTimedTask.getCode(), sysTimedTask.getCode(), sysTimedTask.getCode(), sysTimedTask.getCode());
        } catch (SchedulerException e) {
            logger.warn("删除(禁用)系统定时任务失败", e);
            throw new ApiException(ErrorResponse.create("删除定时任务失败"), e);
        }
        logger.debug("删除系统定时任务数据库记录");
        int i = sysTimedTaskMapper.deleteById(sysTimedTaskId);
        ApiAssert.isEqualToOne(i, ErrorResponse.create("删除定时任务失败"));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void deleteSysTimedTaskByCode(String code) {
        ApiAssert.isNotBlank(code, ErrorResponse.create("系统定时任务标识不能为空"));
        logger.debug("检查系统定时任务是否存在");
        SysTimedTask sysTimedTask = sysTimedTaskMapper.selectOne(Wrappers.<SysTimedTask>lambdaQuery().eq(SysTimedTask::getCode, code));
        ApiAssert.isNotNull(sysTimedTask, ErrorResponse.create("系统系统定时任务不存在"));
        logger.debug("删除系统定时任务");
        try {
            QuartzHelper.deleteJob(code, code, code, code);
        } catch (SchedulerException e) {
            logger.warn("删除(禁用)系统定时任务失败", e);
            throw new ApiException(ErrorResponse.create("删除定时任务失败"), e);
        }
        logger.debug("删除系统定时任务数据库记录");
        int i = sysTimedTaskMapper.delete(Wrappers.<SysTimedTask>lambdaQuery().eq(SysTimedTask::getCode, code));
        ApiAssert.isEqualToOne(i, ErrorResponse.create("删除定时任务失败"));
    }

    @Override
    public void enabledSysTimedTask(String sysTimedTaskId, String operatedSysUserId) {
        ApiAssert.isNotBlank(sysTimedTaskId, ErrorResponse.create("系统定时任务ID不能为空"));
        ApiAssert.isNotBlank(operatedSysUserId, ErrorResponse.create("操作的系统用户ID不能为空"));
        logger.debug("获取系统定时任务信息");
        SysTimedTask sysTimedTask = sysTimedTaskMapper.selectById(sysTimedTaskId);
        ApiAssert.isNotNull(sysTimedTask, ErrorResponse.create("系统定时任务不存在"));
        Class<?> jobClass;
        try {
            jobClass = Class.forName(sysTimedTask.getClassName());
        } catch (ClassNotFoundException e) {
            logger.warn("没有找到系统定时任务class");
            throw new ApiException(ErrorResponse.create(String.format("没有找到系统定时任务类%s", sysTimedTask.getClassName())), e);
        }
        logger.debug("检查jobClass是否是Job.class的子类");
        ApiAssert.isAssignable(Job.class, jobClass, ErrorResponse.create(String.format("系统定时任务%s没有继承Job类", jobClass)));
        Map dataMap = null;
        if (StringUtils.isNotBlank(sysTimedTask.getData())) {
            dataMap = JacksonUtils.parseObject(sysTimedTask.getData(), Map.class);
        }
        logger.debug("添加启用的系统定时任务");
        try {
            QuartzHelper.addJob(sysTimedTask.getCode(), sysTimedTask.getCode(), sysTimedTask.getCode(), sysTimedTask.getCode(),
                    (Class<? extends Job>) jobClass, sysTimedTask.getCronExpression(), dataMap);
        } catch (SchedulerException e) {
            logger.warn("添加(启用)系统定时任务失败", e);
            throw new ApiException(ErrorResponse.create("启用系统定时任务失败"), e);
        }
        logger.debug("修改系统定时任务数据库记录为启用");
        sysTimedTask.setUpdateSysUserId(operatedSysUserId);
        sysTimedTask.setUpdateDateTime(LocalDateTime.now());
        sysTimedTask.setStatus(StatusEnum.ENABLED);
        int i = sysTimedTaskMapper.updateById(sysTimedTask);
        ApiAssert.isGreaterThatZero(i, ErrorResponse.create("启用系统定时任务失败"));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    @SuppressWarnings("unchecked")
    public void enabledSysTimedTaskByCode(String code, String operatedSysUserId) {
        ApiAssert.isNotBlank(code, ErrorResponse.create("系统定时任务标识不能为空"));
        ApiAssert.isNotBlank(operatedSysUserId, ErrorResponse.create("操作的系统用户ID不能为空"));
        logger.debug("获取系统定时任务信息");
        SysTimedTask sysTimedTask = sysTimedTaskMapper.selectOne(Wrappers.<SysTimedTask>lambdaQuery().eq(SysTimedTask::getCode, code));
        ApiAssert.isNotNull(sysTimedTask, ErrorResponse.create("系统定时任务不存在"));
        Class<?> jobClass;
        try {
            jobClass = Class.forName(sysTimedTask.getClassName());
        } catch (ClassNotFoundException e) {
            logger.warn("没有找到系统定时任务class");
            throw new ApiException(ErrorResponse.create(String.format("没有找到系统定时任务类%s", sysTimedTask.getClassName())), e);
        }
        logger.debug("检查jobClass是否是Job.class的子类");
        ApiAssert.isAssignable(Job.class, jobClass, ErrorResponse.create(String.format("系统定时任务%s没有继承Job类", jobClass)));
        Map dataMap = null;
        if (StringUtils.isNotBlank(sysTimedTask.getData())) {
            dataMap = JacksonUtils.parseObject(sysTimedTask.getData(), Map.class);
        }
        logger.debug("添加启用的系统定时任务");
        try {
            QuartzHelper.addJob(sysTimedTask.getCode(), sysTimedTask.getCode(), sysTimedTask.getCode(), sysTimedTask.getCode(),
                    (Class<? extends Job>) jobClass, sysTimedTask.getCronExpression(), dataMap);
        } catch (SchedulerException e) {
            logger.warn("添加(启用)系统定时任务失败", e);
            throw new ApiException(ErrorResponse.create("启用系统定时任务失败"), e);
        }
        logger.debug("修改系统定时任务数据库记录为启用");
        sysTimedTask.setUpdateSysUserId(operatedSysUserId);
        sysTimedTask.setUpdateDateTime(LocalDateTime.now());
        sysTimedTask.setStatus(StatusEnum.ENABLED);
        int i = sysTimedTaskMapper.updateById(sysTimedTask);
        ApiAssert.isGreaterThatZero(i, ErrorResponse.create("启用系统定时任务失败"));
    }

    @Override
    public void disableSysTimedTask(String sysTimedTaskId, String operatedSysUserId) {
        ApiAssert.isNotBlank(sysTimedTaskId, ErrorResponse.create("系统定时任务ID不能为空"));
        ApiAssert.isNotBlank(operatedSysUserId, ErrorResponse.create("操作的系统用户ID不能为空"));
        logger.debug("获取系统定时任务信息");
        SysTimedTask sysTimedTask = sysTimedTaskMapper.selectById(sysTimedTaskId);
        ApiAssert.isNotNull(sysTimedTask, ErrorResponse.create("系统定时任务不存在"));
        logger.debug("删除禁用的数据库记录");
        try {
            QuartzHelper.deleteJob(sysTimedTask.getCode(), sysTimedTask.getCode(), sysTimedTask.getCode(), sysTimedTask.getCode());
        } catch (SchedulerException e) {
            logger.warn("删除(禁用)系统定时任务失败", e);
            throw new ApiException(ErrorResponse.create("禁用定时任务失败"), e);
        }
        logger.debug("修改系统定时任务数据库记录为禁用");
        sysTimedTask.setUpdateSysUserId(operatedSysUserId);
        sysTimedTask.setUpdateDateTime(LocalDateTime.now());
        sysTimedTask.setStatus(StatusEnum.DISABLE);
        int i = sysTimedTaskMapper.updateById(sysTimedTask);
        ApiAssert.isGreaterThatZero(i, ErrorResponse.create("禁用系统定时任务失败"));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void disableSysTimedTaskByCode(String code, String operatedSysUserId) {
        ApiAssert.isNotBlank(code, ErrorResponse.create("系统定时任务标识不能为空"));
        ApiAssert.isNotBlank(operatedSysUserId, ErrorResponse.create("操作的系统用户ID不能为空"));
        logger.debug("获取系统定时任务信息");
        SysTimedTask sysTimedTask = sysTimedTaskMapper.selectOne(Wrappers.<SysTimedTask>lambdaQuery().eq(SysTimedTask::getCode, code));
        ApiAssert.isNotNull(sysTimedTask, ErrorResponse.create("系统定时任务不存在"));
        logger.debug("删除禁用的数据库记录");
        try {
            QuartzHelper.deleteJob(code, code, code, code);
        } catch (SchedulerException e) {
            logger.warn("删除(禁用)系统定时任务失败", e);
            throw new ApiException(ErrorResponse.create("禁用定时任务失败"), e);
        }
        logger.debug("修改系统定时任务数据库记录为禁用");
        sysTimedTask.setUpdateSysUserId(operatedSysUserId);
        sysTimedTask.setUpdateDateTime(LocalDateTime.now());
        sysTimedTask.setStatus(StatusEnum.DISABLE);
        int i = sysTimedTaskMapper.updateById(sysTimedTask);
        ApiAssert.isGreaterThatZero(i, ErrorResponse.create("禁用系统定时任务失败"));
    }

    @Override
    public void executionSysTimedTask(String sysTimedTaskId) {
        ApiAssert.isNotBlank(sysTimedTaskId, ErrorResponse.create("系统定时任务ID不能为空"));
        SysTimedTask sysTimedTask = sysTimedTaskMapper.selectById(sysTimedTaskId);
        ApiAssert.isNotNull(sysTimedTask, ErrorResponse.create("系统定时任务不存在"));
        try {
            logger.debug("执行系统定时任务");
            QuartzHelper.triggerJob(sysTimedTask.getCode(), sysTimedTask.getCode());
        } catch (SchedulerException e) {
            logger.warn("立即执行系统定时任务出错", e);
            //return false;
            throw new ApiException(ErrorResponse.create("立即执行系统定时任务失败"), e);
        }
    }

    @Override
    public void executionSysTimedTaskByCode(String code) {
        ApiAssert.isNotBlank(code, ErrorResponse.create("系统定时任务标识不能为空"));
        try {
            logger.debug("执行系统定时任务");
            QuartzHelper.triggerJob(code, code);
        } catch (SchedulerException e) {
            logger.warn("立即执行系统定时任务出错", e);
            //return false;
            throw new ApiException(ErrorResponse.create("立即执行系统定时任务失败"), e);
        }
    }
}
