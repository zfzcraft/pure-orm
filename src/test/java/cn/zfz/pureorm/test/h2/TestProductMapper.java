package cn.zfz.pureorm.test.h2;

import cn.zfz.pureorm.core.BaseMapper;

/**
 * 自定义 Mapper 接口，通过 MapperFactory.create() 创建动态代理实现
 */
public interface TestProductMapper extends BaseMapper<TestProduct> {
}
