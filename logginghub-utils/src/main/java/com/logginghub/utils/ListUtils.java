package com.logginghub.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

    public static <X,Y> List<X> convert(List<Y> collection, Convertor<X, Y> convertor) {
        List<X> result = new ArrayList<X>();
        for (Y y : collection) {
            X x = convertor.convert(y);
            result.add(x);
        }
        return result;
    }
    
}
