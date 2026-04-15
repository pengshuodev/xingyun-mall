package com.xingyun.orderpayment.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingyun.orderpayment.modules.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
