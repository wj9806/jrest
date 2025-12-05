package io.github.wj9806.jrest.client.proxy;

/**
 * HTTP客户端类型枚举
 */
public enum ClientType {
    /**
     * Apache HttpClient实现
     */
    APACHE("apache"),
    
    /**
     * JDK原生HttpClient实现
     */
    NATIVE("native");
    
    private final String value;
    
    ClientType(String value) {
        this.value = value;
    }
    
    /**
     * 获取枚举值对应的字符串
     *
     * @return 字符串值
     */
    public String getValue() {
        return value;
    }
    
    /**
     * 根据字符串值获取对应的枚举
     *
     * @param value 字符串值
     * @return 对应的枚举，默认返回APACHE
     */
    public static ClientType fromValue(String value) {
        for (ClientType type : ClientType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return APACHE;
    }
}
