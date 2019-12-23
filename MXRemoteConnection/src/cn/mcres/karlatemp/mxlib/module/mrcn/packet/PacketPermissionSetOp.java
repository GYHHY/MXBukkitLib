/*
 * Copyright (c) 2018-2019 Karlatemp. All rights reserved.
 * Reserved.FileName: PacketPermissionSetOp.java@author: karlatemp@vip.qq.com: 19-12-16 下午10:33@version: 2.0
 */

package cn.mcres.karlatemp.mxlib.module.mrcn.packet;

import cn.mcres.karlatemp.mxlib.module.packet.PacketDataSerializer;
import cn.mcres.karlatemp.mxlib.remote.netty.Packet;

public class PacketPermissionSetOp implements Packet<PacketPermissionSetOp> {
    public boolean state;

    @Override
    public void read(PacketDataSerializer serializer) {
        state = serializer.readBoolean();
    }

    @Override
    public void write(PacketDataSerializer serializer) {
        serializer.writeBoolean(state);
    }
}