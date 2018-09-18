package com.eaphonetech.common.datatables.model.config;

import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.eaphonetech.common.datatables.model.mapping.DataTablesInput;

/**
 * {@link HandlerMethodArgumentResolver} to allow injection of {@link DataTablesInput} into
 * Spring MVC controller methods
 * 
 * @author Xiaoyu Guo
 */
public class DataTablesArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (DataTablesInput.class.equals(parameter.getParameterType())) {
            return true;
        }
        return false;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
