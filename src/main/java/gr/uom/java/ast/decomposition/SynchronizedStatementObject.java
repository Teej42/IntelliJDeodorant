package gr.uom.java.ast.decomposition;

import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiSynchronizedStatement;

class SynchronizedStatementObject extends CompositeStatementObject {

    SynchronizedStatementObject(PsiStatement statement, AbstractMethodFragment parent) {
        super(statement, StatementType.SYNCHRONIZED, parent);
        AbstractExpression abstractExpression = new AbstractExpression(
                ((PsiSynchronizedStatement) statement).getLockExpression(), this);
        this.addExpression(abstractExpression);
    }

}
