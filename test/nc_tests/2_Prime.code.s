.data
blank : .asciiz " "
_1sc : .asciiz "Please input a number:\n"
_2sc : .asciiz "This number's fibonacci value is :\n"
_3sc : .asciiz "\nThe number of prime numbers within n is:\n"
.text
__init:
	lui $sp, 0x8000
	addi $sp, $sp, 0x0000
	move $fp, $sp
	add $gp, $gp, 0x8000
	jal main
	li $v0, 10
	syscall
Mars_PrintInt:
	li $v0, 1
	syscall
	li $v0, 4
	move $v1, $a0
	la $a0, blank
	syscall
	move $a0, $v1
	jr $ra
Mars_GetInt:
	li $v0, 5
	syscall
	jr $ra
Mars_PrintStr:
	li $v0, 4
	syscall
	jr $ra
prime:
	subu $sp, $sp, 32
	move $25, $4
	li $23, 0
	move $24, $23
	li $20, 1
	move $21, $20
	li $20, 2
	move $23, $20
_1LoopCheckLabel:
	sle $20, $23, $25
	beq $20, $0, _1LoopEndLabel
	li $20, 1
	move $21, $20
	li $20, 2
	move $22, $20
_2LoopCheckLabel:
	mul $20, $22, $22
	sle $19, $20, $23
	move $20, $19
	beq $20, $0, _2LoopEndLabel
	li $20, 0
	rem $19, $23, $22
	seq $18, $19, $20
	move $20, $18
	beq $20, $0, _1otherwise1
	li $20, 0
	move $21, $20
	j _2LoopEndLabel
	j _1endif
_1otherwise1:
_1endif:
_2LoopStepLabel:
	li $20, 1
	add $22, $22, $20
	j _2LoopCheckLabel
_2LoopEndLabel:
	li $20, 1
	seq $19, $21, $20
	move $20, $19
	beq $20, $0, _2otherwise1
	li $20, 1
	add $24, $24, $20
	sw $20, -4($fp)
	sw $21, -8($fp)
	sw $22, -12($fp)
	sw $23, -16($fp)
	sw $24, -20($fp)
	sw $25, -24($fp)
	subu $sp, $sp, 32
	sw $fp, ($sp)
	move $fp, $sp
	sw $31, 20($sp)
	move $4, $23
	jal Mars_PrintInt
	lw $31, 20($sp)
	lw $fp, ($sp)
	addu $sp, $sp, 32
	lw $20, -4($fp)
	lw $21, -8($fp)
	lw $22, -12($fp)
	lw $23, -16($fp)
	lw $24, -20($fp)
	lw $25, -24($fp)
	j _2endif
_2otherwise1:
_2endif:
_1LoopStepLabel:
	li $20, 1
	add $23, $23, $20
	j _1LoopCheckLabel
_1LoopEndLabel:
	move $2, $24
	move $sp, $fp
	jr $31
main:
	subu $sp, $sp, 32
	la $25, _1sc
	sw $20, -4($fp)
	sw $21, -8($fp)
	sw $22, -12($fp)
	sw $23, -16($fp)
	sw $24, -20($fp)
	sw $25, -24($fp)
	subu $sp, $sp, 32
	sw $fp, ($sp)
	move $fp, $sp
	sw $31, 20($sp)
	move $4, $25
	jal Mars_PrintStr
	lw $31, 20($sp)
	lw $fp, ($sp)
	addu $sp, $sp, 32
	lw $20, -4($fp)
	lw $21, -8($fp)
	lw $22, -12($fp)
	lw $23, -16($fp)
	lw $24, -20($fp)
	lw $25, -24($fp)
	sw $20, -4($fp)
	sw $21, -8($fp)
	sw $22, -12($fp)
	sw $23, -16($fp)
	sw $24, -20($fp)
	sw $25, -24($fp)
	subu $sp, $sp, 32
	sw $fp, ($sp)
	move $fp, $sp
	sw $31, 20($sp)
	jal Mars_GetInt
	lw $31, 20($sp)
	lw $fp, ($sp)
	addu $sp, $sp, 32
	lw $20, -4($fp)
	lw $21, -8($fp)
	lw $22, -12($fp)
	lw $23, -16($fp)
	lw $24, -20($fp)
	lw $25, -24($fp)
	move $24, $2
	move $25, $24
	sw $20, -4($fp)
	sw $21, -8($fp)
	sw $22, -12($fp)
	sw $23, -16($fp)
	sw $24, -20($fp)
	sw $25, -24($fp)
	subu $sp, $sp, 32
	sw $fp, ($sp)
	move $fp, $sp
	sw $31, 20($sp)
	move $4, $25
	jal prime
	lw $31, 20($sp)
	lw $fp, ($sp)
	addu $sp, $sp, 32
	lw $20, -4($fp)
	lw $21, -8($fp)
	lw $22, -12($fp)
	lw $23, -16($fp)
	lw $24, -20($fp)
	lw $25, -24($fp)
	move $23, $2
	move $24, $23
	la $23, _3sc
	sw $20, -4($fp)
	sw $21, -8($fp)
	sw $22, -12($fp)
	sw $23, -16($fp)
	sw $24, -20($fp)
	sw $25, -24($fp)
	subu $sp, $sp, 32
	sw $fp, ($sp)
	move $fp, $sp
	sw $31, 20($sp)
	move $4, $23
	jal Mars_PrintStr
	lw $31, 20($sp)
	lw $fp, ($sp)
	addu $sp, $sp, 32
	lw $20, -4($fp)
	lw $21, -8($fp)
	lw $22, -12($fp)
	lw $23, -16($fp)
	lw $24, -20($fp)
	lw $25, -24($fp)
	sw $20, -4($fp)
	sw $21, -8($fp)
	sw $22, -12($fp)
	sw $23, -16($fp)
	sw $24, -20($fp)
	sw $25, -24($fp)
	subu $sp, $sp, 32
	sw $fp, ($sp)
	move $fp, $sp
	sw $31, 20($sp)
	move $4, $24
	jal Mars_PrintInt
	lw $31, 20($sp)
	lw $fp, ($sp)
	addu $sp, $sp, 32
	lw $20, -4($fp)
	lw $21, -8($fp)
	lw $22, -12($fp)
	lw $23, -16($fp)
	lw $24, -20($fp)
	lw $25, -24($fp)
	li $23, 0
	move $2, $23
	move $sp, $fp
	jr $31
