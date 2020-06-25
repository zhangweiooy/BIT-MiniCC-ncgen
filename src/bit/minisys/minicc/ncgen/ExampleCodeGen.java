package bit.minisys.minicc.ncgen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import bit.minisys.minicc.MiniCCCfg;
import bit.minisys.minicc.icgen.internal.IRBuilder;
import bit.minisys.minicc.icgen.internal.MiniCCICGen;
import bit.minisys.minicc.internal.util.MiniCCUtil;
import bit.minisys.minicc.ncgen.IMiniCCCodeGen;
import bit.minisys.minicc.parser.ast.ASTCompilationUnit;
import bit.minisys.minicc.parser.ast.ASTNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExampleCodeGen implements IMiniCCCodeGen{


	public ExampleCodeGen() {
		
	}
	
	@Override
	public String run(String iFile, MiniCCCfg cfg) throws Exception {
		String oFile = MiniCCUtil.remove2Ext(iFile) + MiniCCCfg.MINICC_CODEGEN_OUTPUT_EXT;
		ObjectMapper mapper = new ObjectMapper();
		ASTCompilationUnit program = (ASTCompilationUnit)mapper.readValue(new File(iFile), ASTCompilationUnit.class);
		if(cfg.target.equals("mips")) {
			//TODO:
			CodeGenerator codeGenerator = new CodeGenerator();
			program.accept(codeGenerator);
			try {
			FileWriter fileWriter = new FileWriter(new File(oFile));
			fileWriter.write(codeGenerator.getMips().toString());
			fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else if (cfg.target.equals("riscv")) {
			//TODO:
		}else if (cfg.target.equals("x86")){
			//TODO:
		}
		
		System.out.println("7. Target code generation finished!");
		
		return oFile;
	}
}
