package core.ast;

import core.ast.decomposition.CatchClauseObject;
import core.ast.decomposition.TryStatementObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public abstract class ClassDeclarationObject {
    String name;
    final List<MethodObject> methodList;
    final List<FieldObject> fieldList;
    final List<CommentObject> commentList;

    ClassDeclarationObject() {
        this.methodList = new ArrayList<>();
        this.fieldList = new ArrayList<>();
        this.commentList = new ArrayList<>();
    }

    public abstract ClassObject getClassObject();

    protected abstract TypeObject getSuperclass();

    public void addMethod(MethodObject method) {
        methodList.add(method);
    }

    public void addField(FieldObject f) {
        fieldList.add(f);
    }

    public void addComment(CommentObject comment) {
        commentList.add(comment);
    }

    public boolean addComments(List<CommentObject> comments) {
        return commentList.addAll(comments);
    }

    public List<MethodObject> getMethodList() {
        return methodList;
    }

    public ListIterator<MethodObject> getMethodIterator() {
        return methodList.listIterator();
    }

    public ListIterator<FieldObject> getFieldIterator() {
        return fieldList.listIterator();
    }

    public ListIterator<CommentObject> getCommentIterator() {
        return commentList.listIterator();
    }

    public int getNumberOfMethods() {
        return methodList.size();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isGeneratedByParserGenerator() {
        for (CommentObject comment : commentList) {
            if (comment.getText().contains("Generated By:JavaCC") ||
                    comment.getText().contains("Generated By:JJTree") ||
                    (comment.getText().contains("Generated from") && comment.getText().contains("by ANTLR")) ||
                    comment.getText().contains("generated by JFlex") ||
                    comment.getText().contains("generated by CUP") ||
                    comment.getText().contains("created by jay")) {
                return true;
            }
        }
        return false;
    }

    public boolean containsMethodWithTestAnnotation() {
        for (MethodObject method : methodList) {
            if (method.hasTestAnnotation())
                return true;
        }
        return false;
    }

    public boolean extendsTestCase() {
        TypeObject superclass = this.getSuperclass();
        if (superclass == null)
            return false;
        else if (superclass.getClassType().equals("junit.framework.TestCase"))
            return true;
        else {
            ClassObject superClassObject = ASTReader.getSystemObject().getClassObject(superclass.getClassType());
            if (superClassObject != null)
                return superClassObject.extendsTestCase();
        }
        return false;
    }

    public MethodObject getMethod(MethodInvocationObject mio) {
        ListIterator<MethodObject> mi = getMethodIterator();
        while (mi.hasNext()) {
            MethodObject mo = mi.next();
            if (mo.equals(mio))
                return mo;
        }
        return null;
    }

    public MethodObject getMethod(SuperMethodInvocationObject smio) {
        ListIterator<MethodObject> mi = getMethodIterator();
        while (mi.hasNext()) {
            MethodObject mo = mi.next();
            if (mo.equals(smio))
                return mo;
        }
        return null;
    }

    public boolean containsMethodInvocation(MethodInvocationObject methodInvocation) {
        for (MethodObject method : methodList) {
            if (method.containsMethodInvocation(methodInvocation))
                return true;
        }
        return false;
    }

    public boolean containsFieldInstruction(FieldInstructionObject fieldInstruction) {
        for (MethodObject method : methodList) {
            if (method.containsFieldInstruction(fieldInstruction))
                return true;
        }
        return false;
    }

    public boolean containsMethodInvocation(MethodInvocationObject methodInvocation, MethodObject excludedMethod) {
        for (MethodObject method : methodList) {
            if (!method.equals(excludedMethod) && method.containsMethodInvocation(methodInvocation))
                return true;
        }
        return false;
    }

    public boolean containsSuperMethodInvocation(SuperMethodInvocationObject superMethodInvocation) {
        for (MethodObject method : methodList) {
            if (method.containsSuperMethodInvocation(superMethodInvocation))
                return true;
        }
        return false;
    }

    public boolean hasFieldType(String className) {
        ListIterator<FieldObject> fi = getFieldIterator();
        while (fi.hasNext()) {
            FieldObject fo = fi.next();
            if (fo.getType().getClassType().equals(className))
                return true;
        }
        return false;
    }

    public Set<FieldObject> getFieldsAccessedInsideMethod(AbstractMethodDeclaration method) {
        Set<FieldObject> fields = new LinkedHashSet<>();
        for (FieldInstructionObject fieldInstruction : method.getFieldInstructions()) {
            FieldObject accessedFieldFromThisClass = findField(fieldInstruction);
            if (accessedFieldFromThisClass != null) {
                fields.add(accessedFieldFromThisClass);
            }
        }
        if (method.getMethodBody() != null) {
            List<TryStatementObject> tryStatements = method.getMethodBody().getTryStatements();
            for (TryStatementObject tryStatement : tryStatements) {
                for (CatchClauseObject catchClause : tryStatement.getCatchClauses()) {
                    for (FieldInstructionObject fieldInstruction : catchClause.getBody().getFieldInstructions()) {
                        FieldObject accessedFieldFromThisClass = findField(fieldInstruction);
                        if (accessedFieldFromThisClass != null) {
                            fields.add(accessedFieldFromThisClass);
                        }
                    }
                }
                if (tryStatement.getFinallyClause() != null) {
                    for (FieldInstructionObject fieldInstruction : tryStatement.getFinallyClause().getFieldInstructions()) {
                        FieldObject accessedFieldFromThisClass = findField(fieldInstruction);
                        if (accessedFieldFromThisClass != null) {
                            fields.add(accessedFieldFromThisClass);
                        }
                    }
                }
            }
        }
        return fields;
    }

    private FieldObject getField(FieldInstructionObject fieldInstruction) {
        for (FieldObject field : fieldList) {
            if (field.equals(fieldInstruction)) {
                return field;
            }
        }
        return null;
    }

    FieldObject findField(FieldInstructionObject fieldInstruction) {
        FieldObject field = getField(fieldInstruction);
        if (field != null) {
            return field;
        } else {
            TypeObject superclassType = getSuperclass();
            if (superclassType != null) {
                ClassObject superclassObject = ASTReader.getSystemObject().getClassObject(superclassType.toString());
                if (superclassObject != null) {
                    return superclassObject.findField(fieldInstruction);
                }
            }
        }
        return null;
    }
}
