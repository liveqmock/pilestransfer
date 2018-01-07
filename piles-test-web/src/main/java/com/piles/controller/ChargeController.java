package com.piles.controller;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.piles.common.entity.BasePushCallBackResponse;
import com.piles.common.entity.type.TradeType;
import com.piles.common.util.ChannelMapByEntity;
import com.piles.control.entity.RemoteStartPushRequest;
import com.piles.control.entity.RemoteStartRequest;
import com.piles.control.service.IRemoteStartPushService;
import com.piles.entity.vo.ChargeRemoteStartRequest;
import com.piles.util.ServiceFactoryUtil;
import com.piles.util.Util;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;

import static com.piles.common.entity.type.EPushResponseCode.READ_OK;

@Slf4j
@Controller
@RequestMapping("/charge")
public class ChargeController {
    @Resource
    ServiceFactoryUtil serviceFactoryUtil;


    /**
     * 启动充电
     *
     * @param remoteStartRequest
     * @return
     */
    @RequestMapping(value = "/charge", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> charge(ChargeRemoteStartRequest remoteStartRequest) {
        log.info("请求充电请求信息:"+JSON.toJSONString(remoteStartRequest));
        Map<String, Object> map = new HashedMap();
        map=checkParams(remoteStartRequest);
        if (null!=map){
            return map;
        }
        if (!(1 == remoteStartRequest.getChargeModel()
                || 2 == remoteStartRequest.getChargeModel()
                || 3 == remoteStartRequest.getChargeModel()
                || 4 == remoteStartRequest.getChargeModel())
                ) {
            map.put("status", "-1");
            map.put("msg", "充电模式不可用");
            log.info("return请求充电请求fan:"+JSON.toJSONString(map));
            return map;
        }

        IRemoteStartPushService iRemoteStartPushService=serviceFactoryUtil.getStartPushService(remoteStartRequest.getTradeTypeCode());
        RemoteStartPushRequest remoteStartPushRequest = new RemoteStartPushRequest();
        remoteStartPushRequest.setTradeTypeCode(remoteStartRequest.getTradeTypeCode());
        remoteStartPushRequest.setGunNo(remoteStartRequest.getGunNo());
        remoteStartPushRequest.setOrderNo(remoteStartRequest.getOrderNo());
        remoteStartPushRequest.setPileNo(remoteStartRequest.getPileNo());
//        remoteStartPushRequest.setPileNo("0000000080000004");
        remoteStartPushRequest.setSerial(Integer.parseInt(remoteStartRequest.getSerial()));
        remoteStartPushRequest.setChargeData(remoteStartRequest.getChargeData());
        remoteStartPushRequest.setChargeModel(remoteStartRequest.getChargeModel());
        remoteStartPushRequest.setChargeStopCode(StringUtils.isEmpty(remoteStartRequest.getChargeStopCode()) ? "6464" : remoteStartRequest.getChargeStopCode());
        BasePushCallBackResponse<RemoteStartRequest> remoteStartRequestBasePushCallBackResponse = iRemoteStartPushService.doPush(remoteStartPushRequest);

        if (remoteStartRequestBasePushCallBackResponse.getCode() != READ_OK) {
            //重试1
            remoteStartRequestBasePushCallBackResponse = iRemoteStartPushService.doPush(remoteStartPushRequest);
        }
        log.info("远程启动充电请求返回报文:{}", JSON.toJSONString(remoteStartRequestBasePushCallBackResponse));

        map.put("status", remoteStartRequestBasePushCallBackResponse.getCode().getCode());

        switch (remoteStartRequestBasePushCallBackResponse.getCode().getCode()) {
            case 200:
                map.put("msg", "启动充电发送命令成功,详细结果见结果");
                map.put("data", remoteStartRequestBasePushCallBackResponse.getObj());
                Util.chargePushOrderOk.put(String.valueOf(remoteStartPushRequest.getSerial()), remoteStartRequestBasePushCallBackResponse.getObj());
                break;
            case 300:
            case 100:
                map.put("status", 300);
                map.put("msg", "请求超时");
                break;
            case 400:
                map.put("msg", "充电桩链接不可用");
                break;
            default:
                break;

        }

        log.info("return请求充电请求fan:"+JSON.toJSONString(map));
        return map;

    }

    /**
     * 启动充电
     *
     * @param remoteStartRequest
     * @return
     */
    @RequestMapping(value = "/appendCharge", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> appendharge(ChargeRemoteStartRequest remoteStartRequest) {
        log.info("请求充电请求信息:"+JSON.toJSONString(remoteStartRequest));
        Map<String, Object> map = new HashedMap();

        map=checkParams(remoteStartRequest);
        if (null!=map){
            return map;
        }
        if (TradeType.XUN_DAO.getCode()==remoteStartRequest.getTradeTypeCode()){
            map.put("status", "-1");
            map.put("msg", "充电桩不支持追加电量");
            log.info("return请求充电请求fan:"+JSON.toJSONString(map));
            return map;
        }

        RemoteStartPushRequest remoteStartPushRequest = new RemoteStartPushRequest();
        remoteStartPushRequest.setTradeTypeCode(remoteStartRequest.getTradeTypeCode());
        remoteStartPushRequest.setGunNo(remoteStartRequest.getGunNo());
        remoteStartPushRequest.setOrderNo(remoteStartRequest.getOrderNo());
        remoteStartPushRequest.setPileNo(remoteStartRequest.getPileNo());
        remoteStartPushRequest.setSerial(Integer.parseInt(remoteStartRequest.getSerial()));
        remoteStartPushRequest.setChargeData(remoteStartRequest.getChargeData());
        remoteStartPushRequest.setChargeModel(5);
        remoteStartPushRequest.setChargeStopCode(StringUtils.isEmpty(remoteStartRequest.getChargeStopCode()) ? "6464" : remoteStartRequest.getChargeStopCode());
        IRemoteStartPushService iRemoteStartPushService=serviceFactoryUtil.getStartPushService(remoteStartRequest.getTradeTypeCode());
        BasePushCallBackResponse<RemoteStartRequest> remoteStartRequestBasePushCallBackResponse = iRemoteStartPushService.doPush(remoteStartPushRequest);
        int i = 0;
        while (remoteStartRequestBasePushCallBackResponse.getCode() != READ_OK) {
            //重试2次 总共调用三次

            remoteStartRequestBasePushCallBackResponse = iRemoteStartPushService.doPush(remoteStartPushRequest);
            i++;
            if (i >= 2) {
                break;
            }
        }

        log.info("远程启动充电追加充电请求返回报文:{}", JSON.toJSONString(remoteStartRequestBasePushCallBackResponse));

        map.put("status", remoteStartRequestBasePushCallBackResponse.getCode().getCode());

        switch (remoteStartRequestBasePushCallBackResponse.getCode().getCode()) {
            case 200:
                map.put("msg", "追加充电发送命令成功,详细结果见结果");
                map.put("data", remoteStartRequestBasePushCallBackResponse.getObj());
                Util.chargePushOrderOk.put(String.valueOf(remoteStartPushRequest.getSerial()), remoteStartRequestBasePushCallBackResponse.getObj());

                break;
            case 300:
            case 100:
                map.put("status", 300);
                map.put("msg", "请求超时");
                break;
            case 400:
                map.put("msg", "充电桩链接不可用");
                break;
            default:
                break;

        }

        log.info("return请求充电请求fan:"+JSON.toJSONString(map));
        return map;

    }

    private Map<String, Object> checkParams(ChargeRemoteStartRequest remoteStartRequest){
        Map<String, Object> map = new HashedMap();
        //check 参数
        int serial=0;


        if (StringUtils.isEmpty(remoteStartRequest.getTradeTypeCode())) {
            map.put("status", "-1");
            map.put("msg", "充电桩厂商类型为空");
            log.info("return请求充电请求fan:"+JSON.toJSONString(map));
            return map;
        }
        if (StringUtils.isEmpty(remoteStartRequest.getPileNo())) {
            map.put("status", "-1");
            map.put("msg", "充电桩编号为空");
            log.info("return请求充电请求fan:"+JSON.toJSONString(map));
            return map;
        }
        if (StringUtils.isEmpty(remoteStartRequest.getSerial())) {
            map.put("status", "-1");
            map.put("msg", "流水号为空");
            log.info("return请求充电请求fan:"+JSON.toJSONString(map));
            return map;
        }
        try {
            serial=Integer.parseInt( remoteStartRequest.getSerial() );
            if (serial>65535){
                map.put("status", "-1");
                map.put("msg", "流水号不能大于65535");
                log.info("return请求充电请求fan:"+JSON.toJSONString(map));
            }
        }catch (Exception e){
            map.put("status", "-1");
            map.put("msg", "流水号需要是数字");
            log.info("return请求充电请求fan:"+JSON.toJSONString(map));
            return map;
        }

        if (StringUtils.isEmpty(remoteStartRequest.getGunNo()) || 1 != remoteStartRequest.getGunNo()) {
            map.put("status", "-1");
            map.put("msg", "充电桩枪号不可用");
            log.info("return请求充电请求fan:"+JSON.toJSONString(map));
            return map;
        }
        if (StringUtils.isEmpty(remoteStartRequest.getOrderNo())) {
            map.put("status", "-1");
            map.put("msg", "订单号不可用");
            log.info("return请求充电请求fan:"+JSON.toJSONString(map));
            return map;
        }

        //获取连接channel 获取不到无法推送
        Channel channel = ChannelMapByEntity.getChannel(remoteStartRequest.getTradeTypeCode(),remoteStartRequest.getPileNo());
        if (null == channel) {
            map.put("status", "400");
            map.put("msg", "充电桩链接不可用");
            log.info("return请求充电请求fan:"+JSON.toJSONString(map));
            return map;
        }
            return null;

    }

}
