package cn.mastercom.nettypush.service;



/**
 * @author sixiaojie
 * @date 2020-03-30-17:06
 */
public interface PushService {
    /**
     * 推送给指定用户
     * @param userId
     * @param msg
     */
    void pushMsgToOne(String userId,String msg);

    /**
     * 推送给所有用户
     * @param msg
     */
    void pushMsgToAll(String msg);
}
