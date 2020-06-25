package bit.minisys.minicc.ncgen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import bit.minisys.minicc.MiniCCCfg;
import bit.minisys.minicc.icgen.Quat;
import bit.minisys.minicc.internal.util.MiniCCUtil;
import bit.minisys.minicc.parser.ast.ASTCompilationUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import bit.minisys.minicc.parser.ast.*;

class regInfo {
	Integer regId;
	String value;
}

public class Example implements IMiniCCCodeGen {

	private Map<ASTNode, ASTNode> map;
    private List<Quat> quats;
    private Integer tmpId;
    private StringBuilder sb = new StringBuilder();
    private int regID;
    private int regCount;
    private regInfo[] regInfos = new regInfo[20];
    private int globalSelectionId = 0;
    private int localLoopId = 0;
    private int globalLoop = 0;
    private Stack<Integer> localLoop = new Stack<>();
    private Stack<String> selectStmt = new Stack<>();
    private Integer selectionId;
    private Integer localSelection = 0;
    private Stack<Integer> localSelectId = new Stack<>();
    private Stack<Integer> globalSelectId = new Stack<>();
    private Stack<Integer> operands = new Stack<Integer>();
    private Stack<Integer> operandType = new Stack<>();

    private void clr() {

    }

    @Override
    public String run(String iFile, MiniCCCfg cfg) throws Exception {
        String oFile = MiniCCUtil.remove2Ext(iFile) + MiniCCCfg.MINICC_CODEGEN_OUTPUT_EXT;
        ObjectMapper mapper = new ObjectMapper();
        ASTCompilationUnit program = (ASTCompilationUnit) mapper.readValue(new File(iFile), ASTCompilationUnit.class);
        Example icBuilder = new Example();
        StringBuilder sb = icBuilder.ICGenerator(program);
		try {
			FileWriter fileWriter = new FileWriter(new File(oFile));
			fileWriter.write(sb.toString());
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        System.out.println("7. Target code generation finished!");

        return oFile;
    }

    private StringBuilder ICGenerator(ASTCompilationUnit program) {
        sb.append(".data\n" +
                "blank : .asciiz \" \"\n" +
                "_1sc : .asciiz \"Please input a number:\\n\"\n" +
                "_2sc : .asciiz \"This number's fibonacci value is :\\n\"\n" +
				"_3sc : .asciiz \"The number of prime numbers within n is:\\n\"\n" +
                ".text\n" +
                "__init:\n" +
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
            if (node instanceof ASTFunctionDefine) {
                regID = 25;
                regCount=0;
				for (int i = 0; i < 20; i++) {
					regInfos[i] = new regInfo();
				}
                visit((ASTFunctionDefine) node);
				for (int i = 0; i < 20; i++) {
					regInfos[i] = new regInfo();
				}
                regCount=0;
                regID = 25;
            }
        }
        return sb;
    }

    private void visit(ASTFunctionDefine functionDefine) {
        ASTDeclarator decl = functionDefine.declarator;
        if (decl instanceof ASTFunctionDeclarator) {
            if (((ASTFunctionDeclarator) decl).declarator instanceof ASTVariableDeclarator) {
                sb.append(((ASTVariableDeclarator) ((ASTFunctionDeclarator) decl).declarator).identifier.value).append(":\n");
            }
            sb.append("\tsubu $sp, $sp, 32\n");
            if (((ASTFunctionDeclarator) decl).params.size()!=0) {
                sb.append("\tmove $25, $4\n");
                regInfos[regCount].value= ((ASTVariableDeclarator) ((ASTFunctionDeclarator) decl).params.get(0).declarator).identifier.value;
                regInfos[regCount].regId=regID;
                regCount++;
                regID--;
            }
            visit(functionDefine.body);
        }
    }

    private void visit(ASTCompoundStatement compoundStat) {
        for (ASTNode node : compoundStat.blockItems) {
            if (node instanceof ASTDeclaration) {
                visit((ASTDeclaration) node);
            } else if (node instanceof ASTStatement) {
                visit((ASTStatement) node);
            }
        }
    }

    private void visit(ASTDeclaration declaration) {
    	for(ASTInitList init:declaration.initLists){
    		if(init.exprs.size()!=0) {
				for (ASTExpression exp : init.exprs) {
					if (exp instanceof ASTFunctionCall) {
						visit((ASTFunctionCall) exp);
					}
					if (init.declarator instanceof ASTVariableDeclarator) {
						regInfos[regCount].value = ((ASTVariableDeclarator) init.declarator).identifier.value;
						regInfos[regCount].regId = regID;
						operands.push(regID);
						operandType.push(0);
						regCount++;
						regID--;
					}
					if (exp instanceof ASTIntegerConstant) {
						sb.append("\tli $").append(regID).append(", ").append(((ASTIntegerConstant) exp).value).append("\n");
					} else {
						sb.append("\tmove $").append(regID).append(" ,$2\n");
					}
					operands.push(regID);
					operandType.push(1);
					regID--;
					int operand1 = operands.pop();
					int operand2 = operands.pop();
					sb.append("\tmove $").append(operand2).append(" ,$").append(operand1).append("\n");
					int p = operandType.pop() + operandType.pop();
					regID = regID + p;
				}
			}else{
    			regInfos[regCount].value=((ASTVariableDeclarator)init.declarator).identifier.value;
    			regInfos[regCount].regId=regID;
    			regID--;
    			regCount+=1;
			}
		}
    }

    private void visit(ASTStatement statement) {
        if (statement instanceof ASTIterationDeclaredStatement) {
            globalLoop++;
            localLoopId = globalLoop;
            localLoop.push(localLoopId);
            visit((ASTIterationDeclaredStatement) statement);
            localLoopId = localLoop.pop();
        } else if (statement instanceof ASTIterationStatement) {
			localLoop.push(localLoopId);
			globalLoop++;
			localLoopId = globalLoop;
            visit((ASTIterationStatement) statement);
			localLoopId = localLoop.pop();
        } else if (statement instanceof ASTCompoundStatement) {
            visit((ASTCompoundStatement) statement);
        } else if (statement instanceof ASTSelectionStatement) {
            globalSelectionId++;
            globalSelectId.push(selectionId);
            selectionId = globalSelectionId;
            localSelectId.push(localSelection);
            localSelection = 0;
            selectStmt.push("_" + selectionId.toString() + "endif:");
            visit((ASTSelectionStatement) statement);
            localSelection = localSelectId.pop();
            selectionId = globalSelectId.pop();
        } else if (statement instanceof ASTExpressionStatement) {
            visit((ASTExpressionStatement) statement);
        } else if (statement instanceof ASTBreakStatement) {
            visit((ASTBreakStatement) statement);
        } else if (statement instanceof ASTContinueStatement) {
            visit((ASTContinueStatement) statement);
        } else if (statement instanceof ASTReturnStatement) {
            visit((ASTReturnStatement) statement);
        } else if (statement instanceof ASTGotoStatement) {
            visit((ASTGotoStatement) statement);
        } else if (statement instanceof ASTLabeledStatement) {
            visit((ASTLabeledStatement) statement);
        }
    }

    private void visit(ASTIterationDeclaredStatement iterationDeclaredStat) {

    }

    private void visit(ASTIterationStatement iterationStatement) {
		if (iterationStatement.init.size() != 0) {
			if (iterationStatement.init.get(0) instanceof ASTBinaryExpression) {
				visit((ASTBinaryExpression) iterationStatement.init.get(0));
			}
		}
		sb.append("_").append(localLoopId).append("LoopCheckLabel:\n");
		if (iterationStatement.cond.size() != 0) {
			if (iterationStatement.cond.get(0) instanceof ASTBinaryExpression) {
				visit((ASTBinaryExpression) iterationStatement.cond.get(0));
			}
			int p = operandType.pop();
			regID = regID + p;
			localSelection++;
			sb.append("\tbeq $").append(operands.pop()).append(", $0, _").append(localLoopId).append("LoopEndLabel\n");
		}
		visit(iterationStatement.stat);
		sb.append("_").append(localLoopId).append("LoopStepLabel:\n");
		if (iterationStatement.step.size() != 0) {
			visit(iterationStatement.step.get(0));
		}
		sb.append("\tj _").append(localLoopId).append("LoopCheckLabel\n");
		sb.append("_").append(localLoopId).append("LoopEndLabel:\n");
    }

    private void visit(ASTSelectionStatement selectionStatement) {
        if (localSelection != 0) {
            sb.append(selectStmt.pop()).append("\n");
        }
        for (ASTExpression cond : selectionStatement.cond) {
            visit(cond);
        }
        int p=operandType.pop();
        regID=regID+p;
		localSelection++;
		sb.append("\tbeq $").append(operands.pop()).append(", $0, _").append(globalSelectionId).append("otherwise").append(localSelection).append("\n");
        selectStmt.push("_" + selectionId.toString() + "otherwise" + localSelection.toString() + ":");
        visit(selectionStatement.then);
        sb.append("\tj _").append(selectionId.toString()).append("endif\n");
        if (selectionStatement.otherwise != null) {
            if (selectionStatement.otherwise instanceof ASTSelectionStatement) {
                visit((ASTSelectionStatement) selectionStatement.otherwise);
            } else {
                sb.append(selectStmt.pop()).append("\n");
                visit((ASTStatement) selectionStatement.otherwise);
                sb.append(selectStmt.pop()).append("\n");
            }
        }else{
        	sb.append(selectStmt.pop()).append("\n");
        	sb.append(selectStmt.pop()).append("\n");
		}
    }

    private void visit(ASTExpressionStatement expressionStatement) {
        for (ASTExpression node : expressionStatement.exprs) {
            if (node instanceof ASTFunctionCall) {
                visit((ASTFunctionCall) node);
            } else
                visit((ASTExpression) node);
        }
    }

    private void visit(ASTExpression expression) {
        if (expression instanceof ASTArrayAccess) {
            visit((ASTArrayAccess) expression);
        } else if (expression instanceof ASTBinaryExpression) {
            visit((ASTBinaryExpression) expression);
        } else if (expression instanceof ASTCastExpression) {
            visit((ASTCastExpression) expression);
        } else if (expression instanceof ASTCharConstant) {
            visit((ASTCharConstant) expression);
        } else if (expression instanceof ASTConditionExpression) {
            visit((ASTConditionExpression) expression);
        } else if (expression instanceof ASTFloatConstant) {
            visit((ASTFloatConstant) expression);
        } else if (expression instanceof ASTFunctionCall) {
            visit((ASTFunctionCall) expression);
        } else if (expression instanceof ASTIdentifier) {
            visit((ASTIdentifier) expression);
        } else if (expression instanceof ASTIntegerConstant) {
            visit((ASTIntegerConstant) expression);
        } else if (expression instanceof ASTMemberAccess) {
            visit((ASTMemberAccess) expression);
        } else if (expression instanceof ASTPostfixExpression) {
            visit((ASTPostfixExpression) expression);
        } else if (expression instanceof ASTStringConstant) {
            visit((ASTStringConstant) expression);
        } else if (expression instanceof ASTUnaryExpression) {
            visit((ASTUnaryExpression) expression);
        } else if (expression instanceof ASTUnaryTypename) {
            visit((ASTUnaryTypename) expression);
        }
    }

    private void visit(ASTFunctionCall funcCall) {
		for(ASTNode ast:funcCall.argList){
			if(ast instanceof ASTStringConstant){
				visit((ASTStringConstant) ast);
			}else if(ast instanceof ASTIntegerConstant){
				visit((ASTIntegerConstant) ast);
			}else if(ast instanceof ASTIdentifier){
				visit((ASTIdentifier) ast);
			}else if(ast instanceof ASTBinaryExpression){
				visit((ASTBinaryExpression) ast);
			}
		}
		sb.append("\tsw $20, -4($fp)\n\tsw $21, -8($fp)\n" +
				"\tsw $22, -12($fp)\n" +
				"\tsw $23, -16($fp)\n" +
				"\tsw $24, -20($fp)\n" +
				"\tsw $25, -24($fp)\n" +
				"\tsubu $sp, $sp, 32\n" +
				"\tsw $fp, ($sp)\n" +
				"\tmove $fp, $sp\n" +
				"\tsw $31, 20($sp)\n");
    	if(funcCall.argList.size()!=0){
    		int p=operandType.pop();
    		sb.append("\tmove $4, $").append(operands.pop()).append("\n");
    		regID=regID+p;
		}
    	sb.append("\tjal ").append(((ASTIdentifier)funcCall.funcname).value).append("\n" +
				"\tlw $31, 20($sp)\n" +
				"\tlw $fp, ($sp)\n" +
				"\taddu $sp, $sp, 32\n");
		sb.append("\tlw $20, -4($fp)\n\tlw $21, -8($fp)\n" +
				"\tlw $22, -12($fp)\n" +
				"\tlw $23, -16($fp)\n" +
				"\tlw $24, -20($fp)\n" +
				"\tlw $25, -24($fp)\n");

    }

    private void visit(ASTStringConstant str){
    	if(str.value.equals("\"Please input a number:\\n\"")){
    		sb.append("\tla $").append(regID).append(", _1sc\n");
    		operands.push(regID);
    		operandType.push(1);
    		regID--;
		}else if(str.value.equals("\"This number's fibonacci value is :\\n\"")){
    		sb.append("\tla $").append(regID).append(", _2sc\n");
			operands.push(regID);
			operandType.push(1);
			regID--;
		}else if(str.value.equals("\"The number of prime numbers within n is:\\n\"")){
    		sb.append("\tla $").append(regID).append(", _3sc\n");
    		operands.push(regID);
    		operandType.push(1);
    		regID--;
		}
	}

	private void visit(ASTIdentifier id){
    	for(int temp=0;temp<regCount;temp++){
    		if(regInfos[temp].value.equals(id.value)){
    			operands.push(regInfos[temp].regId);
    			operandType.push(0);
    			break;
    		}
		}
	}

	private void visit(ASTPostfixExpression postfixExpression){
    	if(postfixExpression.op.value.equals("++")){
    		sb.append("\tli $").append(regID).append(" ,1\n");
    		operands.push(regID);
    		operandType.push(1);
    		regID--;
		}
    	int temp=0;
    	for(;temp<regCount;temp++){
    		if(regInfos[temp].value.equals(((ASTIdentifier)postfixExpression.expr).value)){
				operands.push(regInfos[temp].regId);
				operandType.push(0);
				break;
			}
		}
    	int operand1=operands.pop();
    	int operand2=operands.pop();
    	sb.append("\t").append("add ").append("$");
    	sb.append(operand1).append(", $").append(operand1).append(", $").append(operand2).append("\n");
    	int p=operandType.pop()+operandType.pop();
    	regID=regID+p;
	}

    private void visit(ASTBreakStatement breakStatement) {
		sb.append("\tj _").append(localLoopId).append("LoopEndLabel\n");
    }

    private void visit(ASTContinueStatement continueStatement) {

    }

    private void visit(ASTReturnStatement returnStatement) {
		if(returnStatement.expr.get(0) instanceof ASTIdentifier){
			for(int temp=0;temp<regCount;temp++){
				if(regInfos[temp].value.equals(((ASTIdentifier) returnStatement.expr.get(0)).value)){
					sb.append("\tmove $2, $").append(regInfos[temp].regId).append("\n");
				}
			}
		}else if(returnStatement.expr.get(0) instanceof ASTIntegerConstant) {
			sb.append("\tli $").append(regID).append(", ").append(((ASTIntegerConstant) returnStatement.expr.get(0)).value).append("\n");
			operands.push(regID);
			operandType.push(1);
			int p=operandType.pop();
			regID=regID+p;
			sb.append("\tmove $2, $").append(operands.pop()).append("\n");
		}
		sb.append("\tmove $sp, $fp\n\tjr $31\n");
    }

    private void visit(ASTBinaryExpression binaryExpression) {
		String op=binaryExpression.op.value;
		if (op.equals("=")){
			if (binaryExpression.expr1 instanceof ASTIdentifier){
				int temp = 0;
				for(;temp<regCount;temp++){
					if(regInfos[temp].value.equals(((ASTIdentifier) binaryExpression.expr1).value)){
						operands.push(regInfos[temp].regId);
						operandType.push(0);
						break;
					}
				}
				if(temp==regCount){
					regInfos[regCount].value=((ASTIdentifier) binaryExpression.expr1).value;
					regInfos[regCount].regId=regID;
					operands.push(regID);
					operandType.push(0);
					regCount++;
					regID--;
				}
			}
			if(binaryExpression.expr2 instanceof ASTIntegerConstant){
				sb.append("\tli $").append(regID).append(" ,").append(((ASTIntegerConstant) binaryExpression.expr2).value).append("\n");
				operandType.push(1);
				operands.push(regID);
				regID--;
			}else if(binaryExpression.expr2 instanceof ASTBinaryExpression){
				visit((ASTBinaryExpression) binaryExpression.expr2);
			}
			int p=operandType.pop()+operandType.pop();
			regID=regID+p;
			int operand1=operands.pop();
			int operand2=operands.pop();
			sb.append("\tmove $").append(operand2).append(", $").append(operand1).append("\n");
		}else{
			if(op.equals("<")){
				if(binaryExpression.expr2 instanceof ASTIntegerConstant){
					sb.append("\tli $").append(regID).append(", ").append(((ASTIntegerConstant) binaryExpression.expr2).value).append("\n");
					operands.push(regID);
					operandType.push(1);
					regID--;
				}
				if(binaryExpression.expr1 instanceof ASTIdentifier){
					int temp=0;
					for(;temp<regCount;temp++){
						if(regInfos[temp].value.equals(((ASTIdentifier) binaryExpression.expr1).value)){
							operands.push(regInfos[temp].regId);
							operandType.push(0);
							break;
						}
					}
				}
				sb.append("\tslt $").append(regID).append(", $").append(operands.pop()).append(", $").append(operands.pop()).append("\n");
				int p=operandType.pop()+operandType.pop();
				regID=regID+p;
				sb.append("\tmove $").append(regID).append(" ,$").append(regID - p).append("\n");
				operands.push(regID);
				operandType.push(1);
				regID--;
			}else if(op.equals("<=")){
				if(binaryExpression.expr2 instanceof ASTIntegerConstant){
					sb.append("\tli $").append(regID).append(", ").append(((ASTIntegerConstant) binaryExpression.expr2).value).append("\n");
					operands.push(regID);
					operandType.push(1);
					regID--;
				}else if(binaryExpression.expr2 instanceof ASTIdentifier){
					int temp=0;
					for(;temp<regCount;temp++){
						if(regInfos[temp].value.equals(((ASTIdentifier) binaryExpression.expr2).value)){
							operands.push(regInfos[temp].regId);
							operandType.push(0);
							break;
						}
					}
				}
				if(binaryExpression.expr1 instanceof ASTIdentifier){
					int temp=0;
					for(;temp<regCount;temp++){
						if(regInfos[temp].value.equals(((ASTIdentifier) binaryExpression.expr1).value)){
							operands.push(regInfos[temp].regId);
							operandType.push(0);
							break;
						}
					}
				}else if(binaryExpression.expr1 instanceof ASTBinaryExpression){
					visit((ASTBinaryExpression) binaryExpression.expr1);
				}
				sb.append("\tsle $").append(regID).append(", $").append(operands.pop()).append(", $").append(operands.pop()).append("\n");
				int p=operandType.pop()+operandType.pop();
				regID=regID+p;
				if(p!=0) {
					sb.append("\tmove $").append(regID).append(" ,$").append(regID - p).append("\n");
				}
				operands.push(regID);
				operandType.push(1);
				regID--;
			}else if(op.equals("+")){
				if(binaryExpression.expr1 instanceof ASTFunctionCall){
					visit((ASTFunctionCall) binaryExpression.expr1);
					sb.append("\tmove $").append(regID).append(", $2\n");
					operandType.push(1);
					operands.push(regID);
					regID--;
				}
				if(binaryExpression.expr2 instanceof ASTFunctionCall){
					visit((ASTFunctionCall) binaryExpression.expr2);
					sb.append("\tmove $").append(regID).append(", $2\n");
					operandType.push(1);
					operands.push(regID);
					regID--;
				}
				int operand1=operands.pop();
				int operand2=operands.pop();
				sb.append("\tadd $").append(regID).append(", $").append(operand2).append(", $").append(operand1).append("\n");
				int p=operandType.pop()+operandType.pop();
				regID=regID+p;
				if(p!=0)
					sb.append("\tmove $").append(regID).append(" ,$").append(regID - p).append("\n");
				operands.push(regID);
				operandType.push(1);
				regID--;
			}else if(op.equals("-")){
				if(binaryExpression.expr2 instanceof ASTIntegerConstant){
					sb.append("\tli $").append(regID).append(", ").append(((ASTIntegerConstant) binaryExpression.expr2).value).append("\n");
					operands.push(regID);
					operandType.push(1);
					regID--;
				}
				if(binaryExpression.expr1 instanceof ASTIdentifier){
					int temp=0;
					for(;temp<regCount;temp++){
						if(regInfos[temp].value.equals(((ASTIdentifier) binaryExpression.expr1).value)){
							operands.push(regInfos[temp].regId);
							operandType.push(0);
							break;
						}
					}
				}
				sb.append("\tsub $").append(regID).append(", $").append(operands.pop()).append(", $").append(operands.pop()).append("\n");
				int p= operandType.pop()+operandType.pop();
				regID=regID+p;
				if(p!=0)
					sb.append("\tmove $").append(regID).append(", $").append(regID-p).append("\n");
				operands.push(regID);
				operandType.push(1);
				regID--;
			}else if(op.equals("*")){
				int temp=0;
				if(binaryExpression.expr2 instanceof ASTIdentifier){
					for(;temp<regCount;temp++){
						if(regInfos[temp].value.equals(((ASTIdentifier) binaryExpression.expr2).value)){
							operands.push(regInfos[temp].regId);
							operandType.push(0);
							break;
						}
					}
				}
				if(binaryExpression.expr1 instanceof ASTIdentifier){
					for(temp=0;temp<regCount;temp++){
						if(regInfos[temp].value.equals(((ASTIdentifier) binaryExpression.expr1).value)){
							operands.push(regInfos[temp].regId);
							operandType.push(0);
							break;
						}
					}
				}
				sb.append("\tmul $").append(regID).append(", $").append(operands.pop()).append(", $").append(operands.pop()).append("\n");
				int p=operandType.pop()+operandType.pop();
				regID=regID+p;
				if(p!=0)
					sb.append("\tmove $").append(regID).append(", $").append(regID-p).append("\n");
				operands.push(regID);
				operandType.push(1);
				regID--;
			}else if(op.equals("==")){
				if(binaryExpression.expr2 instanceof ASTIntegerConstant){
					sb.append("\tli $").append(regID).append(", ").append(((ASTIntegerConstant) binaryExpression.expr2).value).append("\n");
					operands.push(regID);
					operandType.push(1);
					regID--;
				}
				if(binaryExpression.expr1 instanceof ASTBinaryExpression){
					visit((ASTBinaryExpression) binaryExpression.expr1);
				}else if(binaryExpression.expr1 instanceof ASTIdentifier){
					for(int temp=0;temp<regCount;temp++){
						if(regInfos[temp].value.equals(((ASTIdentifier)binaryExpression.expr1).value)){
							operands.push(regInfos[temp].regId);
							operandType.push(0);
							break;
						}
					}
				}
				sb.append("\tseq $").append(regID).append(", $").append(operands.pop()).append(", $").append(operands.pop()).append("\n");
				int p=operandType.pop()+operandType.pop();
				regID=regID+p;
				if(p!=0)
					sb.append("\tmove $").append(regID).append(", $").append(regID-p).append("\n");
				operands.push(regID);
				operandType.push(1);
				regID--;
			}else if(op.equals("%")){
				int temp=0;
				if(binaryExpression.expr2 instanceof ASTIdentifier){
					for(;temp<regCount;temp++){
						if(regInfos[temp].value.equals(((ASTIdentifier) binaryExpression.expr2).value)){
							operands.push(regInfos[temp].regId);
							operandType.push(0);
							break;
						}
					}
				}
				if(binaryExpression.expr1 instanceof ASTIdentifier){
					for(temp=0;temp<regCount;temp++) {
						if (regInfos[temp].value.equals(((ASTIdentifier) binaryExpression.expr1).value)) {
							operands.push(regInfos[temp].regId);
							operandType.push(0);
							break;
						}
					}
				}
				sb.append("\trem $").append(regID).append(", $").append(operands.pop()).append(", $").append(operands.pop()).append("\n");
				int p=operandType.pop()+operandType.pop();
				regID=regID+p;
				if(p!=0)
					sb.append("\tmove $").append(regID).append(", $").append(regID-p).append("\n");
				operands.push(regID);
				operandType.push(1);
				regID--;
			}
		}
    }

}
