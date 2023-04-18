package org.dromara.forest.lifecycles.parameter;

import org.dromara.forest.annotation.Header;
import org.dromara.forest.mapping.MappingParameter;
import org.dromara.forest.mapping.MappingVariable;
import org.dromara.forest.reflection.ForestMethod;
import org.dromara.forest.lifecycles.ParameterAnnotationLifeCycle;
import org.dromara.forest.utils.ReflectUtil;
import org.dromara.forest.utils.StringUtil;

import java.util.Map;

/**
 * Forest &#064;Header注解的生命周期
 * @author gongjun[dt_flys@hotmail.com]
 * @since 2020-08-21 1:31
 */
public class HeaderLifeCycle implements ParameterAnnotationLifeCycle<Header, Object> {

    @Override
    public void onParameterInitialized(ForestMethod method, MappingParameter parameter, Header annotation) {
        Map<String, Object> attrs = ReflectUtil.getAttributesFromAnnotation(annotation);
        String defaultValue = (String) attrs.get("defaultValue");
        String name = (String) attrs.get("name");
        if (StringUtil.isNotEmpty(name)) {
            parameter.setName(name);
            MappingVariable variable = new MappingVariable(name, parameter.getType());
            variable.setIndex(parameter.getIndex());
            method.addVariable(name, variable);
            parameter.setObjectProperties(false);
        } else {
            parameter.setObjectProperties(true);
        }
        if (StringUtil.isNotEmpty(defaultValue)) {
            parameter.setDefaultValue(defaultValue);
        }
        parameter.setTarget(MappingParameter.TARGET_HEADER);
        method.addNamedParameter(parameter);
    }

}
