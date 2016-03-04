package com.dank.analysis.interpret;


import com.dank.hook.RSField;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicInterpreter;

import java.util.ArrayList;

/**
 * Created by Greg on 12/22/2015.
 */
public class FieldInterpreter extends BasicInterpreter {

    public String decoder;
    private ArrayList<RSField> fields = new ArrayList<RSField>();
    private Analyzer analyzer;

    public FieldInterpreter(String decoder) {
        this.decoder = decoder;
        this.analyzer = analyzer;
    }


    public void addFieldContainer(RSField fieldContainer) {
        fields.add(fieldContainer);
    }

    public ArrayList<RSField> getFields() {
        return this.fields;
    }
}
