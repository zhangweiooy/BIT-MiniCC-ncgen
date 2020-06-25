# BIT-MiniCC
BIT Mini C Compiler is a C compiler framework in Java for teaching.

# Building & Running
## Requirements
* JDK 1.8 or higher
* Eclipse Mars

## Building & Running
1. Import the project into Eclipse
2. Set the input source file
3. run as Java applications from class BitMiniCC

# Supported targets
1. x86
2. MIPS
3. RISC-V
4. ARM (coming soon)

# Lab. projects
1. Lexical Analysis: input(C code), output(tokens in JSON)
2. Syntactic Analysis: input(tokens in JSON), output(AST in JSON)
3. Semantic Analysis: input(AST in JSON), output(errors)
4. IR Generation: input(AST in JSON), output(IR)
5. Target Code Generation: input(AST in JSON), output(x86/MIPS/RISC-V assembly)

# Correspondence
* Weixing Ji (jwx@bit.edu.cn) 

# Contributor
* 2020: Hang Li
* 2019: Chensheng Yu, Yueyan Zhao
* 2017: Yu Hao
* 2016: Shuofu Ning
* 2015: YiFan Wu


### 基于BIT-MiniCC实现简单的目标代码生成功能

-实现了nc_tests中的1和2两个测试用例，只实现了这两个是因为只有这两个没有用到数组( *^-^)ρ(*╯^╰)
=======