package com.dank.analysis.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.ArrayLoadNode;
import org.objectweb.asm.commons.cfg.tree.node.ConstantNode;
import org.objectweb.asm.commons.cfg.tree.node.ConversionNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.IincNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.node.StoreNode;
import org.objectweb.asm.commons.cfg.tree.node.TypeNode;
import org.objectweb.asm.commons.cfg.tree.node.VariableNode;

/**
 * Project: RS3Injector
 * Time: 00:43
 * Date: 09-02-2015
 * Created by Dogerina.
 */
public abstract class TreeVisitor extends NodeVisitor {

    protected final BasicBlock block;

    public TreeVisitor(final BasicBlock block) {
        this.block = block;
    }

    //if you dont want to override this, use NodeVisitor.
    public abstract boolean validateBlock(final BasicBlock block);

    @Override
    public final boolean validate() {
        return validateBlock(block);
    }

    public void visitAny(AbstractNode n) {
        super.visitAny(n);
    }

    public void visit(AbstractNode n) {
        super.visit(n);
    }

    public void visitCode() {
        super.visitCode();
    }

    public void visitEnd() {
        super.visitEnd();
    }

    public void visitField(FieldMemberNode fmn) {
        super.visitField(fmn);
    }

    public void visitFrame(AbstractNode n) {
        super.visitFrame(n);
    }

    public void visitIinc(IincNode in) {
        super.visitIinc(in);
    }

    public void visitJump(JumpNode jn) {
        super.visitJump(jn);
    }

    public void visitLabel(AbstractNode n) {
        super.visitLabel(n);
    }

    public void visitConversion(ConversionNode cn) {
        super.visitConversion(cn);
    }

    public void visitConstant(ConstantNode cn) {
        super.visitConstant(cn);
    }

    public void visitNumber(NumberNode nn) {
        super.visitNumber(nn);
    }

    public void visitOperation(ArithmeticNode an) {
        super.visitOperation(an);
    }

    public void visitVariable(VariableNode vn) {
        super.visitVariable(vn);
    }

    public void visitLine(AbstractNode n) {
        super.visitLine(n);
    }

    public void visitLookupSwitch(AbstractNode n) {
        super.visitLookupSwitch(n);
    }

    public void visitMethod(MethodMemberNode mmn) {
        super.visitMethod(mmn);
    }

    public void visitMultiANewArray(AbstractNode n) {
        super.visitMultiANewArray(n);
    }

    public void visitTableSwitch(AbstractNode n) {
        super.visitTableSwitch(n);
    }

    public void visitType(TypeNode tn) {
        super.visitType(tn);
    }

    public void visitStore(StoreNode sn) {
        super.visitStore(sn);
    }

    public void visitArrayLoad(ArrayLoadNode aln) {
        super.visitArrayLoad(aln);
    }
}
