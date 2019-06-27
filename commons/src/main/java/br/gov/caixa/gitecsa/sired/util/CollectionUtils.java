package br.gov.caixa.gitecsa.sired.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CollectionUtils {

    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> l = new ArrayList<T>(c);
        Collections.sort(l);
        return l;
    }
}
