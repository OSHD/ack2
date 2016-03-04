package com.dank.util;

import org.pf.text.StringPattern;

/**
 * Created with IntelliJ IDEA.
 * User: Greg
 * Date: 10/11/13
 * Time: 12:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class Wildcard {

    private String methodCard;

    /**
     * <p>
     * (II)V -> (I?)? -> (IB)Z
     *
     * @param methodCard
     */
    public Wildcard(String methodCard) {
        this.methodCard = methodCard;
    }

    public boolean matches(String s) {
        StringPattern pattern = new StringPattern(methodCard);
        pattern.multiCharWildcardMatchesEmptyString(true);
        return pattern.matches(s); // <== returns true
    }
}