@3030
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
AM=M-1
D=M
@THIS
M=D
@3040
D=A
@SP
A=M
M=D
@SP
M=M+1
@SP
AM=M-1
D=M
@THAT
M=D
@32
D=A
@SP
A=M
M=D
@SP
M=M+1
@THIS
D=M
@2
D=D+A
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D
@46
D=A
@SP
A=M
M=D
@SP
M=M+1
@THAT
D=M
@6
D=D+A
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
// add
@SP
AM=M-1
D=M
A=A-1
M=D+M
@THIS
D=M
@2
A=D+A
D=M
@SP
A=M
M=D
@SP
M=M+1
// sub
@SP
AM=M-1
D=M
A=A-1
M=M-D
@THAT
D=M
@6
A=D+A
D=M
@SP
A=M
M=D
@SP
M=M+1
// add
@SP
AM=M-1
D=M
A=A-1
M=D+M
