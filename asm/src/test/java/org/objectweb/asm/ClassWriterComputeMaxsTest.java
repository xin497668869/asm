// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package org.objectweb.asm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * ClassWriter unit tests for COMPUTE_MAXS option with JSR instructions.
 *
 * @author Eric Bruneton
 */
public class ClassWriterComputeMaxsTest {

  // Some local variable numbers used in tests.
  private static final int LOCAL1 = 1;
  private static final int LOCAL2 = 2;
  private static final int LOCAL3 = 3;
  private static final int LOCAL4 = 4;
  private static final int LOCAL5 = 5;

  private ClassWriter classWriter;
  private MethodVisitor methodVisitor;

  // Labels used to generate test cases.
  private final Label start = new Label();
  private final Label label0 = new Label();
  private final Label label1 = new Label();
  private final Label label2 = new Label();
  private final Label label3 = new Label();
  private final Label label4 = new Label();
  private final Label label5 = new Label();
  private final Label label6 = new Label();
  private final Label label7 = new Label();
  private final Label label8 = new Label();
  private final Label label9 = new Label();
  private final Label label10 = new Label();
  private final Label label11 = new Label();
  private final Label label12 = new Label();

  @BeforeEach
  public void setUp() throws Exception {
    init(Opcodes.V1_1);
  }

  private void init(final int classVersion) {
    classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    classWriter.visit(classVersion, Opcodes.ACC_PUBLIC, "C", null, "java/lang/Object", null);
    methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
    methodVisitor.visitCode();
    methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
    methodVisitor.visitMethodInsn(
        Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    methodVisitor.visitInsn(Opcodes.RETURN);
    methodVisitor.visitMaxs(1, 1);
    methodVisitor.visitEnd();
    methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "m", "()V", null, null);
    methodVisitor.visitCode();
    label(start);
  }

  private void nop() {
    methodVisitor.visitInsn(Opcodes.NOP);
  }

  private void push() {
    methodVisitor.visitInsn(Opcodes.ICONST_0);
  }

  private void pop() {
    methodVisitor.visitInsn(Opcodes.POP);
  }

  private void iconst_0() {
    methodVisitor.visitInsn(Opcodes.ICONST_0);
  }

  private void istore(final int var) {
    methodVisitor.visitVarInsn(Opcodes.ISTORE, var);
  }

  private void aload(final int var) {
    methodVisitor.visitVarInsn(Opcodes.ALOAD, var);
  }

  private void iload(final int var) {
    methodVisitor.visitVarInsn(Opcodes.ILOAD, var);
  }

  private void astore(final int var) {
    methodVisitor.visitVarInsn(Opcodes.ASTORE, var);
  }

  private void ret(final int var) {
    methodVisitor.visitVarInsn(Opcodes.RET, var);
  }

  private void athrow() {
    methodVisitor.visitInsn(Opcodes.ATHROW);
  }

  private void aconst_null() {
    methodVisitor.visitInsn(Opcodes.ACONST_NULL);
  }

  private void vreturn() {
    methodVisitor.visitInsn(Opcodes.RETURN);
  }

  private void label(final Label label) {
    methodVisitor.visitLabel(label);
  }

  private void iinc(final int var, final int increment) {
    methodVisitor.visitIincInsn(var, increment);
  }

  private void go(final Label label) {
    methodVisitor.visitJumpInsn(Opcodes.GOTO, label);
  }

  private void jsr(final Label label) {
    methodVisitor.visitJumpInsn(Opcodes.JSR, label);
  }

  private void ifnonnull(final Label label) {
    methodVisitor.visitJumpInsn(Opcodes.IFNONNULL, label);
  }

  private void ifne(final Label label) {
    methodVisitor.visitJumpInsn(Opcodes.IFNE, label);
  }

  private void trycatch(final Label start, final Label end, final Label handler) {
    methodVisitor.visitTryCatchBlock(start, end, handler, null);
  }

  private void assertMaxs(final int maxStack, final int maxLocals) {
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();
    classWriter.visitEnd();
    byte[] classFile = classWriter.toByteArray();
    ClassReader classReader = new ClassReader(classFile);
    classReader.accept(
        new ClassVisitor(Opcodes.ASM5) {
          @Override
          public MethodVisitor visitMethod(
              final int access,
              final String name,
              final String descriptor,
              final String signature,
              final String[] exceptions) {
            if (name.equals("m")) {
              return new MethodVisitor(Opcodes.ASM5) {
                @Override
                public void visitMaxs(final int realMaxStack, final int realMaxLocals) {
                  assertEquals(maxStack, realMaxStack, "maxStack");
                  assertEquals(maxLocals, realMaxLocals, "maxLocals");
                }
              };
            } else {
              return null;
            }
          }
        },
        0);

    TestClassLoader loader = new TestClassLoader();
    try {
      loader.defineClass("C", classFile).newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      fail(e.getMessage());
    }
  }

  private void assertGraph(final String... nodes) {
    Map<String, Set<String>> expected = new HashMap<String, Set<String>>();
    for (String node : nodes) {
      StringTokenizer stringTokenizer = new StringTokenizer(node, "=,");
      String key = stringTokenizer.nextToken();
      Set<String> values = new HashSet<String>();
      while (stringTokenizer.hasMoreTokens()) {
        values.add(stringTokenizer.nextToken());
      }
      expected.put(key, values);
    }

    Map<String, Set<String>> actual = new HashMap<String, Set<String>>();
    Label currentLabel = start;
    while (currentLabel != null) {
      String key = "N" + currentLabel.getOffset();
      Set<String> value = new HashSet<String>();
      Edge outgoingEdge = currentLabel.outgoingEdges;
      if ((currentLabel.flags & Label.FLAG_SUBROUTINE_CALLER) != 0) {
        // Ignore the first outgoing edge of the basic blocks ending with a jsr: these are virtual
        // edges which lead to the instruction just after the jsr, and do not correspond to a
        // possible execution path (see {@link #visitJumpInsn} and
        // {@link Label#FLAG_SUBROUTINE_CALLER}).
        assertNotNull(outgoingEdge);
        outgoingEdge = outgoingEdge.nextEdge;
      }
      while (outgoingEdge != null) {
        value.add("N" + outgoingEdge.successor.getOffset());
        outgoingEdge = outgoingEdge.nextEdge;
      }
      actual.put(key, value);
      currentLabel = currentLabel.nextBasicBlock;
    }

    assertEquals(expected, actual);
  }

  /**
   * Tests a method which has the most basic <code>try{}finally{}</code> form imaginable. That is,
   * repeated one or more times:
   *
   * <pre>
   * public void a() {
   *   int a = 0;
   *   try {
   *     a++;
   *   } finally {
   *     a--;
   *   }
   *   // ... same try {} finally {} repeated 0 or more times ...
   * }
   * </pre>
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 31, 32, 33})
  public void testBasic(final int numSubroutines) {
    for (int i = 0; i < numSubroutines; ++i) {
      iconst_0(); // N0
      istore(1);

      // Body of try block.
      Label k0 = new Label();
      label(k0); // N2
      iinc(1, 1);
      Label k3 = new Label();
      go(k3);

      // Exception handler.
      Label k1 = new Label();
      label(k1); // N8
      astore(3);
      Label k2 = new Label();
      jsr(k2);
      aload(3); // N12
      athrow();

      // Subroutine.
      label(k2); // N14
      astore(2);
      iinc(1, -1);
      push();
      push();
      ret(2);

      // Non-exceptional exit from try block.
      label(k3); // N22
      jsr(k2);
      push(); // N25
      push();
      Label k4 = new Label();
      label(k4); // N27
      vreturn();

      trycatch(k0, k1, k1);
      trycatch(k3, k4, k1);
    }

    assertMaxs(4, 4);
    if (numSubroutines == 1) {
      assertGraph(
          "N0=N2",
          "N2=N22,N8",
          "N8=N14",
          "N12=",
          "N14=N12,N25",
          "N22=N14,N8",
          "N25=N27,N8",
          "N27=");
    }
  }

  /**
   * Tests a method which has an if/else-if w/in the finally clause. More specifically:
   *
   * <pre>
   * public void a() {
   *   int a = 0;
   *   try {
   *       a++;
   *   } finally {
   *     if (a == 0) {
   *       a += 2;
   *     } else {
   *       a += 3;
   *     }
   *   }
   * }
   * </pre>
   */
  @Test
  public void testIfElseInFinally() {
    iconst_0(); // N0
    istore(1);

    // Body of try block.
    label(label0); // N2
    iinc(1, 1);
    go(label5);

    // Exception handler.
    label(label1); // N8
    astore(3);
    jsr(label2);
    push(); // N12
    push();
    aload(3);
    athrow();

    // Subroutine.
    label(label2); // N16
    astore(2);
    push();
    push();
    iload(1);
    ifne(label3);
    iinc(1, 2);
    go(label4);

    label(label3); // N29
    iinc(1, 3);

    label(label4); // N32, common exit.
    ret(2);

    // Non-exceptional exit from try block.
    label(label5); // N34
    jsr(label2);
    label(label6); // N37
    vreturn();

    trycatch(label0, label1, label1);
    trycatch(label5, label6, label1);

    assertMaxs(5, 4);
    assertGraph(
        "N0=N2",
        "N2=N34,N8",
        "N8=N16",
        "N12=",
        "N16=N29,N32",
        "N29=N32",
        "N32=N37,N12",
        "N34=N16,N8",
        "N37=");
  }

  /**
   * Tests a simple nested finally. More specifically:
   *
   * <pre>
   * public void a1() {
   *   int a = 0;
   *   try {
   *     a += 1;
   *   } finally {
   *     try {
   *       a += 2;
   *     } finally {
   *       a += 3;
   *     }
   *   }
   * }
   * </pre>
   */
  @Test
  public void testSimpleNestedFinally() {
    iconst_0(); // N0
    istore(1);

    // Body of try block.
    label(label0); // N2
    iinc(1, 1);
    jsr(label2);
    go(label5); // N8

    // First exception handler.
    label(label1); // N11
    astore(4);
    jsr(label2);
    aload(4); // N16
    athrow();

    // First subroutine.
    label(label2); // N19
    astore(2);
    iinc(1, 2);
    jsr(label4);
    push(); // N26
    push();
    ret(2);

    // Second exception handler.
    label(label3); // N30
    astore(5);
    jsr(label4);
    aload(5); // N35
    athrow();

    // Second subroutine.
    label(label4); // N38
    astore(3);
    push();
    push();
    iinc(1, 3);
    ret(3);

    // On normal exit, try block jumps here.
    label(label5); // N46
    vreturn();

    trycatch(label0, label1, label1);
    trycatch(label2, label3, label3);

    assertMaxs(5, 6);
    assertGraph(
        "N0=N2",
        "N2=N11,N19",
        "N8=N11,N46",
        "N11=N19",
        "N16=",
        "N19=N30,N38",
        "N26=N16,N30,N8",
        "N30=N38",
        "N35=",
        "N38=N26,N35",
        "N46=");
  }

  /**
   * This tests a subroutine which has no ret statement, but ends in a "return" instead.
   *
   * <p>We structure this as a try/finally with a break in the finally. Because the while loop is
   * infinite, it's clear from the byte code that the only path which reaches the RETURN instruction
   * is through the subroutine.
   *
   * <pre>
   * public void a1() {
   *   int a = 0;
   *   while (true) {
   *     try {
   *       a += 1;
   *     } finally {
   *       a += 2;
   *       break;
   *     }
   *   }
   * }
   * </pre>
   */
  @Test
  public void testSubroutineWithNoRet() {
    iconst_0(); // N0
    istore(1);

    // While loop header/try block.
    label(label0); // N2
    iinc(1, 1);
    jsr(label2);
    go(label3); // N8

    // Implicit catch block.
    label(label1); // N11
    astore(2);
    jsr(label2);
    push(); // N15
    push();
    aload(2);
    athrow();

    // Subroutine which does not return.
    label(label2); // N19
    astore(3);
    iinc(1, 2);
    go(label4);

    // End of the loop, goes back to the top.
    label(label3); // N26
    go(label0);

    label(label4); // N29
    vreturn();

    trycatch(label0, label1, label1);

    assertMaxs(1, 4);
    assertGraph(
        "N0=N2", "N2=N11,N19", "N8=N11,N26", "N11=N19", "N15=", "N19=N29", "N26=N2", "N29=");
  }

  /**
   * This tests a subroutine which has no ret statement, but ends in a "return" instead.
   *
   * <pre>
   *   aconst_null
   *   jsr l0
   * l0:
   *   astore 0
   *   astore 0
   *   return
   * </pre>
   */
  @Test
  public void testSubroutineWithNoRet2() {
    aconst_null(); // N0
    jsr(label0);
    nop(); // N4
    label(label0); // N5
    astore(0);
    astore(0);
    vreturn();
    label(label1); // N8
    methodVisitor.visitLocalVariable("i", "I", null, label0, label1, 1);

    assertMaxs(2, 2);
    assertGraph("N0=N5", "N4=N5", "N5=", "N8=");
  }

  /**
   * This tests a subroutine which has no ret statement, but instead exits implicitly by branching
   * to code which is not part of the subroutine. (Sadly, this is legal)
   *
   * <p>We structure this as a try/finally in a loop with a break in the finally. The loop is not
   * trivially infinite, so the RETURN statement is reachable both from the JSR subroutine and from
   * the main entry point.
   *
   * <pre>
   * public void a1() {
   *   int a = 0;
   *   while (null == null) {
   *     try {
   *       a += 1;
   *     } finally {
   *       a += 2;
   *       break;
   *     }
   *   }
   * }
   * </pre>
   */
  @Test
  public void testImplicitExit() {
    iconst_0(); // N0
    istore(1);

    // While loop header.
    label(label0); // N2
    aconst_null();
    ifnonnull(label5);

    // Yry block.
    label(label1); // N6
    iinc(1, 1);
    jsr(label3);
    go(label4); // N12

    // Implicit catch block.
    label(label2); // N15
    astore(2);
    jsr(label3);
    aload(2); // N19
    push();
    push();
    athrow();

    // Subroutine which does not return.
    label(label3); // N23
    astore(3);
    iinc(1, 2);
    go(label5);

    // End of the loop, goes back to the top.
    label(label4); // N30
    go(label1);

    label(label5); // N33
    vreturn();

    trycatch(label1, label2, label2);

    assertMaxs(1, 4);
    assertGraph(
        "N0=N2",
        "N2=N6,N33",
        "N6=N23,N15",
        "N12=N30,N15",
        "N15=N23",
        "N19=",
        "N23=N33",
        "N30=N6",
        "N33=");
  }

  /**
   * Tests a nested try/finally with implicit exit from one subroutine to the other subroutine.
   * Equivalent to the following java code:
   *
   * <pre>
   * void m(boolean b) {
   *   try {
   *     return;
   *   } finally {
   *     while (b) {
   *       try {
   *         return;
   *       } finally {
   *         // NOTE --- this break avoids the second return above (weird)
   *         if (b) {
   *           break;
   *         }
   *       }
   *     }
   *   }
   * }
   * </pre>
   *
   * <p>This example is from the paper, "Subroutine Inlining and Bytecode Abstraction to Simplify
   * Static and Dynamic Analysis" by Cyrille Artho and Armin Biere.
   */
  @Test
  public void testImplicitExitToAnotherSubroutine() {
    iconst_0(); // N0
    istore(1);

    // First try.
    label(label0); // N2
    jsr(label2);
    vreturn(); // N5

    // Exception handler for first try.
    label(label1); // N6
    astore(LOCAL2);
    jsr(label2);
    push(); // N10
    push();
    aload(LOCAL2);
    athrow();

    // First finally handler.
    label(label2); // N14
    astore(LOCAL4);
    push();
    push();
    go(label6);

    // Body of while loop, also second try.
    label(label3); // N21
    jsr(label5);
    vreturn(); // N24

    // Exception handler for second try.
    label(label4); // N25
    astore(LOCAL3);
    push();
    push();
    jsr(label5);
    aload(LOCAL3); // N31
    athrow();

    // Second finally handler.
    label(label5); // N33
    astore(LOCAL5);
    iload(LOCAL1);
    ifne(label7);
    ret(LOCAL5);

    // Test for the while loop.
    label(label6); // N41
    iload(LOCAL1);
    ifne(label3); // falls through to X.

    // Exit from finally block.
    label(label7); // N45
    ret(LOCAL4);

    trycatch(label0, label1, label1);
    trycatch(label3, label4, label4);

    assertMaxs(5, 6);
    assertGraph(
        "N0=N2",
        "N2=N6,N14",
        "N5=N6",
        "N6=N14",
        "N10=",
        "N14=N41",
        "N21=N25,N33",
        "N24=N25",
        "N25=N33",
        "N31=",
        "N33=N31,N45,N24",
        "N41=N45,N21",
        "N45=N5,N10");
  }

  @Test
  public void testImplicitExitToAnotherSubroutine2() {
    iconst_0(); // N0
    istore(1);
    jsr(label0);
    vreturn(); // N5

    label(label0); // N6
    astore(2);
    jsr(label1);
    go(label2); // N10

    label(label1); // N13
    astore(3);
    iload(1);
    ifne(label2);
    ret(3);

    label(label2); // N20
    ret(2);

    assertMaxs(1, 4);
    assertGraph("N0=N6", "N5=", "N6=N13", "N10=N20", "N13=N20,N10", "N20=N5");
  }

  /**
   * This tests a simple subroutine where the control flow jumps back and forth between the
   * subroutine and the caller.
   *
   * <p>This would not normally be produced by a Java compiler.
   */
  @Test
  public void testInterleavedCode() {
    iconst_0(); // N0
    istore(1);
    jsr(label0);
    go(label1); // N5

    // Subroutine 1.
    label(label0); // N8
    astore(2);
    iinc(1, 1);
    go(label2);

    // Second part of main subroutine.
    label(label1); // N15
    iinc(1, 2);
    go(label3);

    // Second part of subroutine 1.
    label(label2); // N21
    iinc(1, 4);
    push();
    push();
    ret(2);

    // Third part of main subroutine.
    label(label3); // N28
    push();
    push();
    vreturn();

    assertMaxs(4, 3);
    assertGraph("N0=N8", "N5=N15", "N8=N21", "N15=N28", "N21=N5", "N28=");
  }

  /**
   * Tests a nested try/finally with implicit exit from one subroutine to the other subroutine, and
   * with a surrounding try/catch thrown in the mix. Equivalent to the following java code:
   *
   * <pre>
   * void m(int b) {
   *   try {
   *     try {
   *       return;
   *     } finally {
   *       while (b) {
   *         try {
   *           return;
   *         } finally {
   *           // NOTE --- this break avoids the second return above (weird)
   *           if (b) {
   *             break;
   *           }
   *         }
   *       }
   *     }
   *   } catch (Exception e) {
   *     b += 3;
   *     return;
   *   }
   * }
   * </pre>
   */
  @Test
  public void testImplicitExitInTryCatch() {
    iconst_0(); // N0
    istore(1);

    // First try.
    label(label0); // N2
    jsr(label2);
    vreturn(); // N5

    // Exception handler for first try.
    label(label1); // N6
    astore(LOCAL2);
    jsr(label2);
    aload(LOCAL2); // N10
    athrow();

    // First finally handler.
    label(label2); // N12
    astore(LOCAL4);
    go(label6);

    // Body of while loop, also second try.
    label(label3); // N17
    jsr(label5);
    push(); // N20
    push();
    vreturn();

    // Exception handler for second try.
    label(label4); // N23
    astore(LOCAL3);
    jsr(label5);
    aload(LOCAL3); // N27
    athrow();

    // Second finally handler.
    label(label5); // N29
    astore(LOCAL5);
    iload(LOCAL1);
    ifne(label7);
    push();
    push();
    ret(LOCAL5);

    // Test for the while loop.
    label(label6); // N39
    iload(LOCAL1);
    ifne(label3); // Falls through.

    // Exit from finally block.
    label(label7); // N43
    ret(LOCAL4);

    // Outermost catch.
    label(label8); // N45
    iinc(LOCAL1, 3);
    vreturn();

    trycatch(label0, label1, label1);
    trycatch(label3, label4, label4);
    trycatch(label0, label8, label8);

    assertMaxs(4, 6);
    assertGraph(
        "N0=N2",
        "N2=N6,N45,N12",
        "N5=N6,N45",
        "N6=N45,N12",
        "N10=N45",
        "N12=N39,N45",
        "N17=N23,N45,N29",
        "N20=N23,N45",
        "N23=N45,N29",
        "N27=N45",
        "N29=N43,N45,N20,N27",
        "N39=N43,N45,N17",
        "N43=N45,N5,N10",
        "N45=");
  }

  /**
   * Tests an example coming from distilled down version of
   * com/sun/corba/ee/impl/protocol/CorbaClientDelegateImpl from GlassFish 2. See issue #317823.
   */
  @Test
  public void testGlassFish2CorbaClientDelegateImplExample() {
    label(label0); // N0
    jsr(label4);
    label(label1); // N3
    go(label5);
    label(label2); // N6
    pop();
    jsr(label4);
    label(label3); // N10
    aconst_null();
    athrow();
    label(label4); // N12
    astore(1);
    ret(1);
    label(label5); // N15
    aconst_null();
    aconst_null();
    aconst_null();
    pop();
    pop();
    pop();
    label(label6); // N21
    go(label8);
    label(label7); // N24
    pop();
    go(label8);
    aconst_null();
    athrow();
    label(label8); // N30
    iconst_0();
    ifne(label0);
    jsr(label12);
    label(label9); // N37
    vreturn();
    label(label10); // N38
    pop();
    jsr(label12);
    label(label11); // N42
    aconst_null();
    athrow();
    label(label12); // N44
    astore(2);
    ret(2);

    trycatch(label0, label1, label2);
    trycatch(label2, label3, label2);
    trycatch(label0, label6, label7);
    trycatch(label0, label9, label10);
    trycatch(label10, label11, label10);

    assertMaxs(3, 3);
    assertGraph(
        "N0=N6,N12,N24,N38",
        "N3=N15,N24,N38",
        "N6=N6,N12,N24,N38",
        "N10=N24,N38",
        "N12=N3,N10,N24,N38",
        "N15=N21,N24,N38",
        "N21=N30,N38",
        "N24=N30,N38",
        "N30=N0,N38,N44",
        "N37=",
        "N38=N38,N44",
        "N42=",
        "N44=N37,N42");
  }

  /**
   * Tests a nested subroutine with implicit exit from the nested subroutine to the outer one, with
   * the second subroutine coming first in the bytecode instructions sequence.
   */
  @Test
  public void testImplicitExitToAnotherSubroutineInverted() {
    go(label3); // N0
    // Second subroutine, returns to caller of first subroutine.
    label(label0); // N3
    astore(2);
    label(label1); // N4
    ret(1);
    // First subroutine.
    label(label2); // N6
    astore(1);
    aload(0);
    ifnonnull(label1);
    jsr(label0); // This JSR never returns, the following code is unreachable.
    aconst_null(); // N14
    aconst_null();
    aconst_null();
    vreturn();
    // Main "subroutine".
    label(label3); // N18
    jsr(label2);
    label(label4); // N21
    vreturn();

    assertMaxs(1, 3);
    assertGraph("N0=N18", "N3=N4", "N4=N21", "N6=N3,N4", "N14=", "N18=N6", "N21=");
  }

  /**
   * Tests computing the maximum stack size from the existing stack map frames and the instructions
   * in between, when dead code is present.
   */
  @Test
  public void testComputeMaxsFromFramesWithDeadCode() {
    init(Opcodes.V1_7);

    vreturn();
    // With the default compute maxs algorithm, this dead code block is not considered for the
    // maximum stack size, which works fine for classes up to V1_6. Starting with V1_7, stack map
    // frames are mandatory, even for dead code, and the maximum stack size must take dead code into
    // account. Hopefully it can be computed from the stack map frames, and the instructions in
    // between (without any control flow graph construction or algorithm).
    label(label0);
    methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    aconst_null();
    vreturn();

    assertMaxs(1, 1);
  }

  @Test
  public void testComputeMaxsFromFrameWithLong() {
    init(Opcodes.V1_7);

    methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "ABC", "get", "()J", false);
    go(label0);
    label(label0);
    methodVisitor.visitFrame(Opcodes.F_NEW, 0, null, 1, new Object[] {Opcodes.LONG});
    aconst_null();
    vreturn();

    assertMaxs(3, 1);
  }

  private static class TestClassLoader extends ClassLoader {

    public TestClassLoader() {}

    public Class<?> defineClass(final String name, final byte[] classFile) {
      return defineClass(name, classFile, 0, classFile.length);
    }
  }
}
