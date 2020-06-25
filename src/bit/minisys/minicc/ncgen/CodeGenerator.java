package bit.minisys.minicc.ncgen;

import java.util.ArrayList;
import java.util.Stack;

import bit.minisys.minicc.parser.ast.ASTArrayAccess;
import bit.minisys.minicc.parser.ast.ASTArrayDeclarator;
import bit.minisys.minicc.parser.ast.ASTBinaryExpression;
import bit.minisys.minicc.parser.ast.ASTBreakStatement;
import bit.minisys.minicc.parser.ast.ASTCastExpression;
import bit.minisys.minicc.parser.ast.ASTCharConstant;
import bit.minisys.minicc.parser.ast.ASTCompilationUnit;
import bit.minisys.minicc.parser.ast.ASTCompoundStatement;
import bit.minisys.minicc.parser.ast.ASTConditionExpression;
import bit.minisys.minicc.parser.ast.ASTContinueStatement;
import bit.minisys.minicc.parser.ast.ASTDeclaration;
import bit.minisys.minicc.parser.ast.ASTDeclarator;
import bit.minisys.minicc.parser.ast.ASTExpression;
import bit.minisys.minicc.parser.ast.ASTExpressionStatement;
import bit.minisys.minicc.parser.ast.ASTFloatConstant;
import bit.minisys.minicc.parser.ast.ASTFunctionCall;
import bit.minisys.minicc.parser.ast.ASTFunctionDeclarator;
import bit.minisys.minicc.parser.ast.ASTFunctionDefine;
import bit.minisys.minicc.parser.ast.ASTGotoStatement;
import bit.minisys.minicc.parser.ast.ASTIdentifier;
import bit.minisys.minicc.parser.ast.ASTInitList;
import bit.minisys.minicc.parser.ast.ASTIntegerConstant;
import bit.minisys.minicc.parser.ast.ASTIterationDeclaredStatement;
import bit.minisys.minicc.parser.ast.ASTIterationStatement;
import bit.minisys.minicc.parser.ast.ASTLabeledStatement;
import bit.minisys.minicc.parser.ast.ASTMemberAccess;
import bit.minisys.minicc.parser.ast.ASTNode;
import bit.minisys.minicc.parser.ast.ASTParamsDeclarator;
import bit.minisys.minicc.parser.ast.ASTPostfixExpression;
import bit.minisys.minicc.parser.ast.ASTReturnStatement;
import bit.minisys.minicc.parser.ast.ASTSelectionStatement;
import bit.minisys.minicc.parser.ast.ASTStatement;
import bit.minisys.minicc.parser.ast.ASTStringConstant;
import bit.minisys.minicc.parser.ast.ASTToken;
import bit.minisys.minicc.parser.ast.ASTTypename;
import bit.minisys.minicc.parser.ast.ASTUnaryExpression;
import bit.minisys.minicc.parser.ast.ASTUnaryTypename;
import bit.minisys.minicc.parser.ast.ASTVariableDeclarator;
import bit.minisys.minicc.parser.ast.ASTVisitor;

class Register{
	Integer id;
	String value;
}

public class CodeGenerator implements ASTVisitor{

	private StringBuffer mips;//目标代码
	private int registerId;
	private int registerCount;
	private ArrayList<Register> registers;//寄存器的属性
	private Stack<Integer> opnds;
	private Stack<Integer> opndType;
	private int iterationFlag;
	private int iterationId;
	private Stack<Integer> iterations;
	private int selectionFlag;
	private int selectionId;
	private Stack<Integer> selections;
	private int localSelectionId;
	private Stack<Integer> localSelections;
	private Stack<String> selectionStatement;
	public CodeGenerator() {
		// TODO Auto-generated constructor stub
		this.mips = new StringBuffer();
		this.registerId = 0;
		this.registerCount = 0;
		this.registers = new ArrayList<Register>();
		this.opnds = new Stack<Integer>();
		this.opndType = new Stack<Integer>();
		this.iterationFlag = 0;
		this.iterationId = 0;
		this.iterations = new Stack<Integer>();
		this.selectionFlag = 0;
		this.selectionId = 0;
		this.selections = new Stack<Integer>();
		this.localSelectionId = 0;
		this.localSelections = new Stack<Integer>();
		this.selectionStatement = new Stack<String>();
	}
	
	public StringBuffer getMips() {
		return mips;
	}

	@Override
	public void visit(ASTCompilationUnit program) throws Exception {
		// 添加生成时的前置代码（把BIT-MiniCC内置生成的前面部分复制过来）
		this.mips.append(".data\n" + "blank : .asciiz \" \"\n" +
                "_1sc : .asciiz \"Please input a number:\\n\"\n" +
                "_2sc : .asciiz \"This number's fibonacci value is :\\n\"\n" +
				"_3sc : .asciiz \"\\nThe number of prime numbers within n is:\\n\"\n" +
                ".text\n" + "__init:\n" +
                "\tlui $sp, 0x8000\n" +
                "\taddi $sp, $sp, 0x0000\n" +
                "\tmove $fp, $sp\n" +
                "\tadd $gp, $gp, 0x8000\n" +
                "\tjal main\n" +
                "\tli $v0, 10\n" +
                "\tsyscall\n" +
                "Mars_PrintInt:\n" +
                "\tli $v0, 1\n" +
                "\tsyscall\n" +
                "\tli $v0, 4\n" +
                "\tmove $v1, $a0\n" +
                "\tla $a0, blank\n" +
                "\tsyscall\n" +
                "\tmove $a0, $v1\n" +
                "\tjr $ra\n" +
                "Mars_GetInt:\n" +
                "\tli $v0, 5\n" +
                "\tsyscall\n" +
                "\tjr $ra\n" +
                "Mars_PrintStr:\n" +
                "\tli $v0, 4\n" +
                "\tsyscall\n" +
                "\tjr $ra\n");
		
		for (ASTNode node : program.items) {
			if (node instanceof ASTDeclaration) {
				visit((ASTDeclaration)node);
			}else if(node instanceof ASTFunctionDefine){
				this.registerCount = 0;
				this.registerId = 25;
				for (int i = 0; i < 25; i++) {
					this.registers.add(new Register());
				}
				visit((ASTFunctionDefine)node);
				this.registers.clear();
				this.registerCount = 0;
				this.registerId = 25;
			}
		}
		
	}

	@Override
	public void visit(ASTDeclaration declaration) throws Exception {
		// TODO Auto-generated method stub
		for (ASTInitList iterable : declaration.initLists) {		
			if (!iterable.exprs.isEmpty()) {//带初始化的定义
				for (ASTExpression expr : iterable.exprs) {
					if (expr instanceof ASTFunctionCall) {//先遍历函数调用
						visit((ASTFunctionCall)expr);
					}
					if (iterable.declarator instanceof ASTVariableDeclarator) {
						this.registers.get(this.registerCount).id = this.registerId;
						this.registers.get(this.registerCount).value = ((ASTVariableDeclarator)iterable.declarator).identifier.value;
						this.opnds.push(this.registerId);
						this.opndType.push(0);
						this.registerCount++;
						this.registerId--;
					}
					if (expr instanceof ASTIdentifier) {
						this.mips.append("\tmove $");
						this.mips.append(this.registerId);
						this.mips.append(", $2\n");
					}else if(expr instanceof ASTIntegerConstant) {
						this.mips.append("\tli $");
						this.mips.append(this.registerId);
						this.mips.append(", ");
						this.mips.append(((ASTIntegerConstant)expr).value);
						this.mips.append("\n");
					}else  {
						this.mips.append("\tmove $");
						this.mips.append(this.registerId);
						this.mips.append(", $2\n");
					}
					this.opnds.push(this.registerId);
					this.opndType.push(1);
					this.registerId--;
					int opnd1 = this.opnds.pop();
					int opnd2 = this.opnds.pop();
					this.mips.append("\tmove $");
					this.mips.append(opnd2);
					this.mips.append(", $");
					this.mips.append(opnd1);
					this.mips.append("\n");
					this.registerId += this.opndType.pop()+this.opndType.pop();
				}
			}else {
				this.registers.get(this.registerCount).id = this.registerId;
				this.registers.get(this.registerCount).value = ((ASTVariableDeclarator)iterable.declarator).identifier.value;
				this.registerId--;
				this.registerCount++;
			}
		}
	}

	@Override
	public void visit(ASTArrayDeclarator arrayDeclarator) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTVariableDeclarator variableDeclarator) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTFunctionDeclarator functionDeclarator) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTParamsDeclarator paramsDeclarator) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTArrayAccess arrayAccess) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTBinaryExpression binaryExpression) throws Exception {
		// TODO Auto-generated method stub
		String op = binaryExpression.op.value;
		if (op.equals("=")) {
			if (binaryExpression.expr1 instanceof ASTIdentifier) {
				int i;
				for (i = 0; i < this.registerCount; i++) {
					if (this.registers.get(i).value.equals(((ASTIdentifier)binaryExpression.expr1).value)) {
						this.opnds.push(this.registers.get(i).id);
						this.opndType.push(0);
						break;
					}
				}
				if (i == this.registerCount) {
					this.registers.get(i).value = ((ASTIdentifier)binaryExpression.expr1).value;
					this.registers.get(i).id = this.registerId;
					this.opnds.push(this.registerId);
					this.opndType.push(0);
					this.registerCount++;
					this.registerId--;
				}
			}
			if (binaryExpression.expr2 instanceof ASTBinaryExpression) {
				visit((ASTBinaryExpression)binaryExpression.expr2);
			}else if (binaryExpression.expr2 instanceof ASTIntegerConstant) {
				this.mips.append("\tli $" + this.registerId + ", " + ((ASTIntegerConstant)binaryExpression.expr2).value + "\n");
				this.opndType.push(1);
				this.opnds.push(this.registerId);
				this.registerId--;
			}
			this.registerId += this.opndType.pop() + this.opndType.pop();
			int opnd1 = this.opnds.pop();
			this.mips.append("\tmove $" + this.opnds.pop() + ", $" + opnd1 + "\n");
		}else {
			if (op.equals("<")) {
				if (binaryExpression.expr2 instanceof ASTIntegerConstant) {
					this.mips.append("\tli $" + this.registerId + ", " + ((ASTIntegerConstant)binaryExpression.expr2).value + "\n");
					this.opnds.push(this.registerId);
					this.opndType.push(1);
					this.registerId--;
				}else if(binaryExpression.expr2 instanceof ASTIdentifier){
					for (int i = 0; i < this.registerCount; i++) {
						if (this.registers.get(i).value.equals(((ASTIdentifier)binaryExpression.expr2).value)) {
							this.opnds.push(this.registers.get(i).id);
							this.opndType.push(0);
							break;
						}
					}
				}
				if (binaryExpression.expr1 instanceof ASTIdentifier) {
					for (int i = 0; i < this.registerCount; i++) {
						if (this.registers.get(i).value.equals(((ASTIdentifier)binaryExpression.expr1).value)) {
							this.opnds.push(this.registers.get(i).id);
							this.opndType.push(0);
							break;
						}
					}
				}else if (binaryExpression.expr1 instanceof ASTBinaryExpression) {
					visit((ASTBinaryExpression)binaryExpression.expr1);
				}
				this.mips.append("\tslt $" + this.registerId + ", $" + this.opnds.pop() + ", $" + this.opnds.pop() + "\n");
				int temp = this.registerId;
				this.registerId += this.opndType.pop() + this.opndType.pop();
				this.mips.append("\tmove $" + this.registerId + ", $" + temp + "\n");
				this.opnds.push(this.registerId);
				this.opndType.push(1);
				this.registerId--;
			}else if (op.equals("<=")) {
				if (binaryExpression.expr2 instanceof ASTIntegerConstant) {
					this.mips.append("\tli $" + this.registerId + ", " + ((ASTIntegerConstant)binaryExpression.expr2).value + "\n");
					this.opnds.push(this.registerId);
					this.opndType.push(1);
					this.registerId--;
				}else if(binaryExpression.expr2 instanceof ASTIdentifier){
					for (int i = 0; i < this.registerCount; i++) {
						if (this.registers.get(i).value.equals(((ASTIdentifier)binaryExpression.expr2).value)) {
							this.opnds.push(this.registers.get(i).id);
							this.opndType.push(0);
							break;
						}
					}
				}
				if (binaryExpression.expr1 instanceof ASTIdentifier) {
					for (int i = 0; i < this.registerCount; i++) {
						if (this.registers.get(i).value.equals(((ASTIdentifier)binaryExpression.expr1).value)) {
							this.opnds.push(this.registers.get(i).id);
							this.opndType.push(0);
							break;
						}
					}
				}else if (binaryExpression.expr1 instanceof ASTBinaryExpression) {
					visit((ASTBinaryExpression)binaryExpression.expr1);
				}
				this.mips.append("\tsle $" + this.registerId + ", $" + this.opnds.pop() + ", $" + this.opnds.pop() + "\n");
				int temp = this.registerId;
				this.registerId += this.opndType.pop() + this.opndType.pop();
				if (temp != this.registerId) {
					this.mips.append("\tmove $" + this.registerId + ", $" + temp + "\n");
				}
				this.opnds.push(this.registerId);
				this.opndType.push(1);
				this.registerId--;
			}else if (op.equals("==")) {
				if (binaryExpression.expr2 instanceof ASTIntegerConstant) {
					this.mips.append("\tli $" + this.registerId + ", " + ((ASTIntegerConstant)binaryExpression.expr2).value + "\n");
					this.opnds.push(this.registerId);
					this.opndType.push(1);
					this.registerId--;
				}else if(binaryExpression.expr2 instanceof ASTIdentifier){
					for (int i = 0; i < this.registerCount; i++) {
						if (this.registers.get(i).value.equals(((ASTIdentifier)binaryExpression.expr2).value)) {
							this.opnds.push(this.registers.get(i).id);
							this.opndType.push(0);
							break;
						}
					}
				}
				if (binaryExpression.expr1 instanceof ASTIdentifier) {
					for (int i = 0; i < this.registerCount; i++) {
						if (this.registers.get(i).value.equals(((ASTIdentifier)binaryExpression.expr1).value)) {
							this.opnds.push(this.registers.get(i).id);
							this.opndType.push(0);
							break;
						}
					}
				}else if (binaryExpression.expr1 instanceof ASTBinaryExpression) {
					visit((ASTBinaryExpression)binaryExpression.expr1);
				}
				this.mips.append("\tseq $" + this.registerId + ", $" + this.opnds.pop() + ", $" + this.opnds.pop() + "\n");
				int temp = this.registerId;
				this.registerId += this.opndType.pop() + this.opndType.pop();
				if (temp != this.registerId) {
					this.mips.append("\tmove $" + this.registerId + ", $" + temp + "\n");
				}
				this.opnds.push(this.registerId);
				this.opndType.push(1);
				this.registerId--;
			}else if (op.equals("+")) {
				if (binaryExpression.expr1 instanceof ASTIdentifier) {
					for (int i = 0; i < this.registerCount; i++) {
						if (this.registers.get(i).value.equals(((ASTIdentifier)binaryExpression.expr1).value)) {
							this.opnds.push(this.registers.get(i).id);
							this.opndType.push(0);
							break;
						}
					}
				}else if (binaryExpression.expr1 instanceof ASTBinaryExpression) {
					visit((ASTBinaryExpression)binaryExpression.expr1);
				}else if (binaryExpression.expr1 instanceof ASTFunctionCall) {
					visit((ASTFunctionCall)binaryExpression.expr1);
					this.mips.append("\tmove $" + this.registerId + ", $2\n");
					this.opnds.push(this.registerId);
					this.opndType.push(1);
					this.registerId--;
				}
				if (binaryExpression.expr2 instanceof ASTIntegerConstant) {
					this.mips.append("\tli $" + this.registerId + ", " + ((ASTIntegerConstant)binaryExpression.expr2).value + "\n");
					this.opnds.push(this.registerId);
					this.opndType.push(1);
					this.registerId--;
				}else if(binaryExpression.expr2 instanceof ASTIdentifier){
					for (int i = 0; i < this.registerCount; i++) {
						if (this.registers.get(i).value.equals(((ASTIdentifier)binaryExpression.expr2).value)) {
							this.opnds.push(this.registers.get(i).id);
							this.opndType.push(0);
							break;
						}
					}
				}else if (binaryExpression.expr2 instanceof ASTFunctionCall) {
					visit((ASTFunctionCall)binaryExpression.expr2);
					this.mips.append("\tmove $" + this.registerId + ", $2\n");
					this.opnds.push(this.registerId);
					this.opndType.push(1);
					this.registerId--;
				}
				int opnd1 = this.opnds.pop();
				this.mips.append("\tadd $" + this.registerId + ", $" + this.opnds.pop() + ", $" + opnd1 + "\n");
				int temp = this.registerId;
				this.registerId += this.opndType.pop() + this.opndType.pop();
				if (temp != this.registerId) {
					this.mips.append("\tmove $" + this.registerId + ", $" + temp + "\n");
				}
				this.opnds.push(this.registerId);
				this.opndType.push(1);
				this.registerId--;
			}else if (op.equals("-")) {
				if (binaryExpression.expr2 instanceof ASTIntegerConstant) {
					this.mips.append("\tli $" + this.registerId + ", " + ((ASTIntegerConstant)binaryExpression.expr2).value + "\n");
					this.opnds.push(this.registerId);
					this.opndType.push(1);
					this.registerId--;
				}else if(binaryExpression.expr2 instanceof ASTIdentifier){
					for (int i = 0; i < this.registerCount; i++) {
						if (this.registers.get(i).value.equals(((ASTIdentifier)binaryExpression.expr2).value)) {
							this.opnds.push(this.registers.get(i).id);
							this.opndType.push(0);
							break;
						}
					}
				}
				if (binaryExpression.expr1 instanceof ASTIdentifier) {
					for (int i = 0; i < this.registerCount; i++) {
						if (this.registers.get(i).value.equals(((ASTIdentifier)binaryExpression.expr1).value)) {
							this.opnds.push(this.registers.get(i).id);
							this.opndType.push(0);
							break;
						}
					}
				}else if (binaryExpression.expr1 instanceof ASTBinaryExpression) {
					visit((ASTBinaryExpression)binaryExpression.expr1);
				}
				this.mips.append("\tsub $" + this.registerId + ", $" + this.opnds.pop() + ", $" + this.opnds.pop() + "\n");
				int temp = this.registerId;
				this.registerId += this.opndType.pop() + this.opndType.pop();
				if (temp != this.registerId) {
					this.mips.append("\tmove $" + this.registerId + ", $" + temp + "\n");
				}
				this.opnds.push(this.registerId);
				this.opndType.push(1);
				this.registerId--;
			}else if (op.equals("*")) {
				if (binaryExpression.expr2 instanceof ASTIntegerConstant) {
					this.mips.append("\tli $" + this.registerId + ", " + ((ASTIntegerConstant)binaryExpression.expr2).value + "\n");
					this.opnds.push(this.registerId);
					this.opndType.push(1);
					this.registerId--;
				}else if(binaryExpression.expr2 instanceof ASTIdentifier){
					for (int i = 0; i < this.registerCount; i++) {
						if (this.registers.get(i).value.equals(((ASTIdentifier)binaryExpression.expr2).value)) {
							this.opnds.push(this.registers.get(i).id);
							this.opndType.push(0);
							break;
						}
					}
				}
				if (binaryExpression.expr1 instanceof ASTIdentifier) {
					for (int i = 0; i < this.registerCount; i++) {
						if (this.registers.get(i).value.equals(((ASTIdentifier)binaryExpression.expr1).value)) {
							this.opnds.push(this.registers.get(i).id);
							this.opndType.push(0);
							break;
						}
					}
				}else if (binaryExpression.expr1 instanceof ASTBinaryExpression) {
					visit((ASTBinaryExpression)binaryExpression.expr1);
				}
				this.mips.append("\tmul $" + this.registerId + ", $" + this.opnds.pop() + ", $" + this.opnds.pop() + "\n");
				int temp = this.registerId;
				this.registerId += this.opndType.pop() + this.opndType.pop();
				if (temp != this.registerId) {
					this.mips.append("\tmove $" + this.registerId + ", $" + temp + "\n");
				}
				this.opnds.push(this.registerId);
				this.opndType.push(1);
				this.registerId--;
			}else if (op.equals("/")) {
				if (binaryExpression.expr2 instanceof ASTIntegerConstant) {
					this.mips.append("\tli $" + this.registerId + ", " + ((ASTIntegerConstant)binaryExpression.expr2).value + "\n");
					this.opnds.push(this.registerId);
					this.opndType.push(1);
					this.registerId--;
				}else if(binaryExpression.expr2 instanceof ASTIdentifier){
					for (int i = 0; i < this.registerCount; i++) {
						if (this.registers.get(i).value.equals(((ASTIdentifier)binaryExpression.expr2).value)) {
							this.opnds.push(this.registers.get(i).id);
							this.opndType.push(0);
							break;
						}
					}
				}
				if (binaryExpression.expr1 instanceof ASTIdentifier) {
					for (int i = 0; i < this.registerCount; i++) {
						if (this.registers.get(i).value.equals(((ASTIdentifier)binaryExpression.expr1).value)) {
							this.opnds.push(this.registers.get(i).id);
							this.opndType.push(0);
							break;
						}
					}
				}else if (binaryExpression.expr1 instanceof ASTBinaryExpression) {
					visit((ASTBinaryExpression)binaryExpression.expr1);
				}
				this.mips.append("\tdiv $" + this.registerId + ", $" + this.opnds.pop() + ", $" + this.opnds.pop() + "\n");
				int temp = this.registerId;
				this.registerId += this.opndType.pop() + this.opndType.pop();
				if (temp != this.registerId) {
					this.mips.append("\tmove $" + this.registerId + ", $" + temp + "\n");
				}
				this.opnds.push(this.registerId);
				this.opndType.push(1);
				this.registerId--;
			}else if (op.equals("%")) {
				if (binaryExpression.expr2 instanceof ASTIntegerConstant) {
					this.mips.append("\tli $" + this.registerId + ", " + ((ASTIntegerConstant)binaryExpression.expr2).value + "\n");
					this.opnds.push(this.registerId);
					this.opndType.push(1);
					this.registerId--;
				}else if(binaryExpression.expr2 instanceof ASTIdentifier){
					for (int i = 0; i < this.registerCount; i++) {
						if (this.registers.get(i).value.equals(((ASTIdentifier)binaryExpression.expr2).value)) {
							this.opnds.push(this.registers.get(i).id);
							this.opndType.push(0);
							break;
						}
					}
				}
				if (binaryExpression.expr1 instanceof ASTIdentifier) {
					for (int i = 0; i < this.registerCount; i++) {
						if (this.registers.get(i).value.equals(((ASTIdentifier)binaryExpression.expr1).value)) {
							this.opnds.push(this.registers.get(i).id);
							this.opndType.push(0);
							break;
						}
					}
				}else if (binaryExpression.expr1 instanceof ASTBinaryExpression) {
					visit((ASTBinaryExpression)binaryExpression.expr1);
				}
				this.mips.append("\trem $" + this.registerId + ", $" + this.opnds.pop() + ", $" + this.opnds.pop() + "\n");
				int temp = this.registerId;
				this.registerId += this.opndType.pop() + this.opndType.pop();
				if (temp != this.registerId) {
					this.mips.append("\tmove $" + this.registerId + ", $" + temp + "\n");
				}
				this.opnds.push(this.registerId);
				this.opndType.push(1);
				this.registerId--;
			}
		}
	}

	@Override
	public void visit(ASTBreakStatement breakStat) throws Exception {
		// TODO Auto-generated method stub
		this.mips.append("\tj _" + this.iterationId + "LoopEndLabel\n");
	}

	@Override
	public void visit(ASTContinueStatement continueStatement) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTCastExpression castExpression) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTCharConstant charConst) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTCompoundStatement compoundStat) throws Exception {
		// TODO Auto-generated method stub
		for (ASTNode node : compoundStat.blockItems) {
			if(node instanceof ASTDeclaration) {
				visit((ASTDeclaration)node);
			}else if (node instanceof ASTStatement) {
				visit((ASTStatement)node);
			}
		}
	}

	@Override
	public void visit(ASTConditionExpression conditionExpression) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTExpression expression) throws Exception {
		// TODO Auto-generated method stub
		if(expression instanceof ASTArrayAccess) {
			visit((ASTArrayAccess)expression);
		}else if(expression instanceof ASTBinaryExpression) {
			visit((ASTBinaryExpression)expression);
		}else if(expression instanceof ASTCastExpression) {
			visit((ASTCastExpression)expression);
		}else if(expression instanceof ASTCharConstant) {
			visit((ASTCharConstant)expression);
		}else if(expression instanceof ASTConditionExpression) {
			visit((ASTConditionExpression)expression);
		}else if(expression instanceof ASTFloatConstant) {
			visit((ASTFloatConstant)expression);
		}else if(expression instanceof ASTFunctionCall) {
			visit((ASTFunctionCall)expression);
		}else if(expression instanceof ASTIdentifier) {
			visit((ASTIdentifier)expression);
		}else if(expression instanceof ASTIntegerConstant) {
			visit((ASTIntegerConstant)expression);
		}else if(expression instanceof ASTMemberAccess) {
			visit((ASTMemberAccess)expression);
		}else if(expression instanceof ASTPostfixExpression) {
			visit((ASTPostfixExpression)expression);
		}else if(expression instanceof ASTStringConstant) {
			visit((ASTStringConstant)expression);
		}else if(expression instanceof ASTUnaryExpression) {
			visit((ASTUnaryExpression)expression);
		}else if(expression instanceof ASTUnaryTypename){
			visit((ASTUnaryTypename)expression);
		}
	}

	@Override
	public void visit(ASTExpressionStatement expressionStat) throws Exception {
		// TODO Auto-generated method stub
		for (ASTExpression node : expressionStat.exprs) {
			visit((ASTExpression)node);
		}
	}

	@Override
	public void visit(ASTFloatConstant floatConst) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTFunctionCall funcCall) throws Exception {
		// TODO Auto-generated method stub
		for (ASTNode node : funcCall.argList) {
			if(node instanceof ASTStringConstant){
				visit((ASTStringConstant)node);
			}else if(node instanceof ASTIntegerConstant){
				visit((ASTIntegerConstant)node);
			}else if(node instanceof ASTIdentifier){
				visit((ASTIdentifier)node);
			}else if(node instanceof ASTBinaryExpression){
				visit((ASTBinaryExpression)node);
			}
		}
		this.mips.append("\tsw $20, -4($fp)\n" + 
				"\tsw $21, -8($fp)\n" +
				"\tsw $22, -12($fp)\n" +
				"\tsw $23, -16($fp)\n" +
				"\tsw $24, -20($fp)\n" +
				"\tsw $25, -24($fp)\n" +
				"\tsubu $sp, $sp, 32\n" +
				"\tsw $fp, ($sp)\n" +
				"\tmove $fp, $sp\n" +
				"\tsw $31, 20($sp)\n");
		if (!funcCall.argList.isEmpty()) {
			this.mips.append("\tmove $4, $" + this.opnds.pop() + "\n");
			this.registerId += this.opndType.pop();
		}
		this.mips.append("\tjal " + ((ASTIdentifier)funcCall.funcname).value + "\n");
		this.mips.append("\tlw $31, 20($sp)\n" +
				"\tlw $fp, ($sp)\n" +
				"\taddu $sp, $sp, 32\n" +
				"\tlw $20, -4($fp)\n" + 
				"\tlw $21, -8($fp)\n" +
				"\tlw $22, -12($fp)\n" +
				"\tlw $23, -16($fp)\n" +
				"\tlw $24, -20($fp)\n" +
				"\tlw $25, -24($fp)\n");
	}

	@Override
	public void visit(ASTGotoStatement gotoStat) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTIdentifier identifier) throws Exception {
		// TODO Auto-generated method stub
		for (int i = 0; i < this.registerCount; i++) {
			if (this.registers.get(i).value.equals(identifier.value)) {
				this.opnds.push(this.registers.get(i).id);
				this.opndType.push(0);
				break;
			}
		}
	}

	@Override
	public void visit(ASTInitList initList) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTIntegerConstant intConst) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTIterationDeclaredStatement iterationDeclaredStat) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTIterationStatement iterationStat) throws Exception {
		// TODO Auto-generated method stub
		for (ASTExpression iterable : iterationStat.init) {
			visit(iterable);
		}
		this.mips.append("_");
		this.mips.append(this.iterationId);
		this.mips.append("LoopCheckLabel:\n");
		for (ASTExpression iterable : iterationStat.cond) {
			visit(iterable);
			this.registerId += this.opndType.pop();
			this.localSelectionId++;
			this.mips.append("\tbeq $");
			this.mips.append(this.opnds.pop());
			this.mips.append(", $0, _");
			this.mips.append(this.iterationId);
			this.mips.append("LoopEndLabel\n");
		}
		visit((ASTCompoundStatement)iterationStat.stat);
		this.mips.append("_");
		this.mips.append(this.iterationId);
		this.mips.append("LoopStepLabel:\n");
		for (ASTExpression iterable : iterationStat.step) {
			visit(iterable);
		}
		this.mips.append("\tj _");
		this.mips.append(this.iterationId);
		this.mips.append("LoopCheckLabel\n");
		this.mips.append("_");
		this.mips.append(this.iterationId);
		this.mips.append("LoopEndLabel:\n");
	}

	@Override
	public void visit(ASTLabeledStatement labeledStat) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTMemberAccess memberAccess) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTPostfixExpression postfixExpression) throws Exception {
		// TODO Auto-generated method stub
		if (postfixExpression.op.value.equals("++")) {
			this.mips.append("\tli $" + this.registerId + ", 1\n");
			this.opnds.push(this.registerId);
			this.opndType.push(1);
			this.registerId--;
		}
		for (int i = 0; i < this.registerCount; i++) {
			if (this.registers.get(i).value.equals(((ASTIdentifier)postfixExpression.expr).value)) {
				this.opnds.push(this.registers.get(i).id);
				this.opndType.push(0);
				break;
			}
		}
		this.mips.append("\tadd $");
		int opnd1 = this.opnds.pop();
		this.mips.append(opnd1 + ", $" + opnd1 + ", $" + this.opnds.pop() + "\n");
		this.registerId += this.opndType.pop() + this.opndType.pop();
	}

	@Override
	public void visit(ASTReturnStatement returnStat) throws Exception {
		// TODO Auto-generated method stub
		if (returnStat.expr.get(0) instanceof ASTIdentifier) {
			for (int i = 0; i < this.registerCount; i++) {
				if (this.registers.get(i).value.equals(((ASTIdentifier)returnStat.expr.get(0)).value)) {
					this.mips.append("\tmove $2, $" + this.registers.get(i).id + "\n");
				}
			}
		}else if (returnStat.expr.get(0) instanceof ASTIntegerConstant) {
			this.mips.append("\tli $" + this.registerId + ", " + ((ASTIntegerConstant)returnStat.expr.get(0)).value + "\n");
			this.opnds.push(this.registerId);
			this.opndType.push(1);
			this.registerId += this.opndType.pop();
			this.mips.append("\tmove $2, $" + this.opnds.pop() + "\n");
		}
		this.mips.append("\tmove $sp, $fp\n" + "\tjr $31\n");
	}

	@Override
	public void visit(ASTSelectionStatement selectionStat) throws Exception {
		// TODO Auto-generated method stub
		if (this.localSelectionId != 0) {
			this.mips.append(this.selectionStatement.pop());
			this.mips.append("\n");
		}
		for (ASTExpression iterable : selectionStat.cond) {
			visit(iterable);
		}
		this.registerId += this.opndType.pop();
		this.localSelectionId++;
		this.mips.append("\tbeq $");
		this.mips.append(this.opnds.pop());
		this.mips.append(", $0, _");
		this.mips.append(this.selectionFlag);
		this.mips.append("otherwise");
		this.mips.append(this.localSelectionId);
		this.mips.append("\n");
		this.selectionStatement.push("_" + this.selectionId + "otherwise" + this.localSelectionId + ":");
		visit(selectionStat.then);
		this.mips.append("\tj _");
		this.mips.append(this.selectionId);
		this.mips.append("endif\n");
		if (selectionStat.otherwise != null) {
			if (selectionStat.otherwise instanceof ASTSelectionStatement) {
				visit((ASTSelectionStatement)selectionStat.otherwise);
			}else {
				this.mips.append(this.selectionStatement.pop());
				this.mips.append("\n");
				visit((ASTStatement)selectionStat.otherwise);
				this.mips.append(this.selectionStatement.pop());
				this.mips.append("\n");
			}
		}else {
			this.mips.append(this.selectionStatement.pop());
			this.mips.append("\n");
			this.mips.append(this.selectionStatement.pop());
			this.mips.append("\n");
		}
	}

	@Override
	public void visit(ASTStringConstant stringConst) throws Exception {
		// TODO Auto-generated method stub
		if (stringConst.value.equals("\"Please input a number:\\n\"")) {
			this.mips.append("\tla $" + this.registerId + ", _1sc\n");
			this.opnds.push(this.registerId);
			this.opndType.push(1);
			this.registerId--;
		}else if (stringConst.value.equals("\"This number's fibonacci value is :\\n\"")) {
			this.mips.append("\tla $" + this.registerId + ", _2sc\n");
			this.opnds.push(this.registerId);
			this.opndType.push(1);
			this.registerId--;
		}else if (stringConst.value.equals("\"The number of prime numbers within n is:\\n\"")) {
			this.mips.append("\tla $" + this.registerId + ", _3sc\n");
			this.opnds.push(this.registerId);
			this.opndType.push(1);
			this.registerId--;
		}
	}

	@Override
	public void visit(ASTTypename typename) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTUnaryExpression unaryExpression) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTUnaryTypename unaryTypename) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTFunctionDefine functionDefine) throws Exception {
		// TODO Auto-generated method stub
		this.mips.append(((ASTVariableDeclarator)((ASTFunctionDeclarator)functionDefine.declarator).declarator).identifier.value);
		this.mips.append(":\n");//添加函数名:
		this.mips.append("\tsubu $sp, $sp, 32\n");
		
		for (ASTParamsDeclarator e : (((ASTFunctionDeclarator)functionDefine.declarator).params)) {//遍历函数的参数	
			this.mips.append("\tmove $25, $4\n");
			this.registers.get(this.registerCount).value = ((ASTVariableDeclarator)(e.declarator)).identifier.value;
			this.registers.get(this.registerCount).id = this.registerId;
			this.registerId--;
			this.registerCount++;
		}
		visit(functionDefine.body);//函数体
	}

	@Override
	public void visit(ASTDeclarator declarator) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ASTStatement statement) throws Exception {
		// TODO Auto-generated method stub
		if(statement instanceof ASTIterationDeclaredStatement) {
			visit((ASTIterationDeclaredStatement)statement);
		}else if(statement instanceof ASTIterationStatement) {
			this.iterationFlag++;
			this.iterations.push(this.iterationId);
			this.iterationId = this.iterationFlag;
			visit((ASTIterationStatement)statement);
			this.iterationId = this.iterations.pop();
			if (this.iterationFlag > 0  ) {
			//	if (this.breakFlag == false) {
			//		this.iterationFlag--;
			//	}else {
					
			//	}
			}else {
			//	if (this.breakFlag == true) {
					
			//	}else {
				//	this.errors.add("_break_not_in_loop.c");
			//	}
			}
		}else if(statement instanceof ASTCompoundStatement) {
			visit((ASTCompoundStatement)statement);
		}else if(statement instanceof ASTSelectionStatement) {
			this.selectionFlag++;
			this.selections.push(this.selectionId);
			this.selectionId = this.selectionFlag;
			this.localSelections.push(this.localSelectionId);
			this.localSelectionId = 0;
			this.selectionStatement.push("_" + this.selectionId + "endif:");
			visit((ASTSelectionStatement)statement);
			this.localSelectionId = this.localSelections.pop();
			this.selectionId = this.selections.pop();
		}else if(statement instanceof ASTExpressionStatement) {
			visit((ASTExpressionStatement)statement);
		}else if(statement instanceof ASTBreakStatement) {
			visit((ASTBreakStatement)statement);
		}else if(statement instanceof ASTContinueStatement) {
			visit((ASTContinueStatement)statement);
		}else if(statement instanceof ASTReturnStatement) {
			visit((ASTReturnStatement)statement);
		}else if(statement instanceof ASTGotoStatement) {
			visit((ASTGotoStatement)statement);
		}else if(statement instanceof ASTLabeledStatement) {
			visit((ASTLabeledStatement)statement);
		}
	}

	@Override
	public void visit(ASTToken token) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
