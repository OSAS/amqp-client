package com.redhat.osas.amqp.client.api;

import java.util.Map;
import java.util.UUID;

public interface Message {
    String getReplyTo();
    void setReplyTo(String replyTo);

    String getSubject();
    void setSubject(String subject);

    String getContentType();
    void setContentType(String contentType);
    
    UUID getMessageId();
    void setMessageId(UUID messageId);
    
    String getUserId();
    void setUserId(String userId);
    
    String getCorrelationId();
    void setCorrelationId(String correlationId);
    
    Integer getPriority();
    void setPriority(Integer priority);
    
    Long getTtl();
    void setTtl(Long ttl);
    
    boolean isDurable();
    void setDurable(boolean durable);
    
    boolean isRedelivered();
    
    Map<String, Object> getProperties();
    void setProperties(Map<String, Object> properties);
    
    byte[] getContent();
    void setContent(byte[] content);
    void setContent(String content);

    long getContentSize();
}
