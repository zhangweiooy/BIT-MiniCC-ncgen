package bit.minisys.minicc.icgen;

public interface IMiniCCICGen {
	/*
	 * @return String the path of the output file
	 * @param iFile input file path
	 */
	public String run(String iFile) throws Exception;
}
