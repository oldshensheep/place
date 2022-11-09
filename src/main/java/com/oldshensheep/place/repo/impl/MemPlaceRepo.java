package com.oldshensheep.place.repo.impl;

import com.oldshensheep.place.config.AppConfig;
import com.oldshensheep.place.repo.PlaceRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemPlaceRepo implements PlaceRepository {

    private final byte[] data;

    public MemPlaceRepo(AppConfig appConfig) {
        data = new byte[appConfig.getByteNum()];
    }

    @Override
    public void setOne(byte[] value, Integer offset) {
        System.arraycopy(value, 0, data, offset, value.length);
    }

    @Override
    public byte[] getOne(int offset, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(data, offset, bytes, 0, length);
        return bytes;
    }

    @Override
    public byte[] getAll() {
        return data;
    }

    @Override
    public void setAll(byte[] value) {
        System.arraycopy(value, 0, data, 0, value.length);
    }


}
