package com.oldshensheep.place.repo;

public interface PlaceRepository {
    void setOne(byte[] value, Integer offset);

    byte[] getOne(int offset, int length);

    byte[] getAll();

    void setAll(byte[] value);
}
