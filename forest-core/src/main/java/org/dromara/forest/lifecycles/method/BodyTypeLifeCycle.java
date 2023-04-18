package org.dromara.forest.lifecycles.method;

import org.dromara.forest.annotation.BodyType;
import org.dromara.forest.converter.ForestEncoder;
import org.dromara.forest.lifecycles.MethodAnnotationLifeCycle;
import org.dromara.forest.reflection.ForestMethod;
import org.dromara.forest.reflection.MetaRequest;
import org.dromara.forest.utils.ReflectUtil;

import java.util.Map;

public class BodyTypeLifeCycle implements MethodAnnotationLifeCycle<BodyType, Object> {

    @Override
    public void onMethodInitialized(ForestMethod method, BodyType annotation) {
        Map<String, Object> attrs = ReflectUtil.getAttributesFromAnnotation(annotation);
        String type = (String) attrs.get("type");
        Object encodeClass = attrs.get("encoder");
        MetaRequest metaRequest = method.getMetaRequest();
        if (metaRequest == null) {
            return;
        }
        metaRequest.setBodyType(type);
        if (encodeClass != null
                && encodeClass instanceof Class
                && !((Class<?>) encodeClass).isInterface()
                && ForestEncoder.class.isAssignableFrom((Class<?>) encodeClass)) {
            metaRequest.setEncoder((Class<? extends ForestEncoder>) encodeClass);
        }
    }


}
