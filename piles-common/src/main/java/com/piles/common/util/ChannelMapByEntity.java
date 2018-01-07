package com.piles.common.util;


import com.piles.common.entity.ChannelEntity;
import com.piles.common.entity.type.TradeType;
import io.netty.channel.Channel;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class ChannelMapByEntity {

    private ChannelMapByEntity() {
    }

    /**
     * key:桩编号,value:channel
     */
    private static Map<String, Channel> channelMap2 = new HashedMap(  );

    /**
     * key:channel,value:桩编号
     */
    private static Map<Channel, String> channelMap3 =  new HashedMap(  );

    /**
     *
     * add channelInfo
     *
     * @param channel
     * @return void
     * @exception
     */
    public static void addChannel(ChannelEntity channelEntity, Channel channel) {
        channelMap2.put(channelEntity.getTradeType().getCode()+"_"+channelEntity.getPileNo(), channel);
    }

    public static void addChannel(Channel channel,ChannelEntity channelEntity) {
        channelMap3.put(channel, channelEntity.getTradeType().getCode()+"_"+channelEntity.getPileNo());
    }


    /**
     * 根据桩编号移出channel
     */
    public static void removeChannel(ChannelEntity channelEntity) {
        Channel channel = channelMap2.get(channelEntity.getTradeType().getCode()+"_"+channelEntity.getPileNo());
        if (channel != null) {
            channelMap3.remove(channel);
        }
        channelMap2.remove(channelEntity.getTradeType().getCode()+"_"+channelEntity.getPileNo());
    }
    /**
     * 根据桩编号移出channel
     */
    public static void removeChannel(int tradeTypeCode,String pileNo) {
        Channel channel = channelMap2.get(tradeTypeCode+"_"+pileNo);
        if (channel != null) {
            channelMap3.remove(channel);
        }
        channelMap2.remove(tradeTypeCode+"_"+pileNo);
    }

    public static Channel getChannel(ChannelEntity channelEntity) {
        return channelMap2.get(channelEntity.getTradeType().getCode()+"_"+channelEntity.getPileNo());
    }
    public static Channel getChannel(int tradeTypeCode,String pileNo) {
        return channelMap2.get(tradeTypeCode+"_"+pileNo);
    }
    public static ChannelEntity getChannel(Channel channel) {
        String temp= channelMap3.get(channel);
        if (StringUtils.isNotEmpty(temp)){
            String[] strings=temp.split("_");
            ChannelEntity channelEntity=new ChannelEntity(strings[1], TradeType.fromCode(Integer.parseInt(strings[0])));
            return channelEntity;
        }
        return null;
    }


    public static void removeChannel(Channel channel) {
        String pileNo = channelMap3.get(channel);
        if (pileNo != null) {
            channelMap2.remove(pileNo);
        }

        channelMap3.remove(channel);
    }
}