/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.env.python;

import com.intellij.ide.hierarchy.HierarchyBrowserBaseEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.env.PyEnvTestCase;
import com.jetbrains.env.python.debug.PyDebuggerTask;
import com.jetbrains.env.python.hierarchy.PyCallHierarchyTask;
import com.jetbrains.python.PythonTestUtil;
import com.jetbrains.python.hierarchy.HierarchyTreeStructureViewer;
import com.jetbrains.python.hierarchy.call.PyCalleeFunctionTreeStructure;
import com.jetbrains.python.hierarchy.call.PyCallerFunctionTreeStructure;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyRecursiveElementVisitor;
import org.jdom.Document;

import java.io.File;


public class PythonCallHierarchyTest extends PyEnvTestCase {

  private static final String TARGET_FUNCTION_NAME = "target_func";
  private static final String CALLER_VERIFICATION_SUFFIX = "_caller_verification.xml";
  private static final String CALLEE_VERIFICATION_SUFFIX = "_callee_verification.xml";
  private static final String WORKING_FOLDER = "/hierarchy/call/";

  private String getBasePath() {
    return "/hierarchy/call/" + getTestName(false);
  }

  private static String getTestDataPath() {
    return PythonTestUtil.getTestDataPath();
  }

  private String getVerificationFilePath(final String suffix) {
    return getTestDataPath() + "/" + getBasePath() + "/" + getTestName(false) + suffix;
  }

  private String getVerificationCallerFilePath() {
    return getVerificationFilePath(CALLER_VERIFICATION_SUFFIX);
  }

  private String getVerificationCalleeFilePath() {
    return getVerificationFilePath(CALLEE_VERIFICATION_SUFFIX);
  }

  private void doCallHierarchyTest(String scriptName) throws Exception {
    final PyDebuggerTask task = new PyDebuggerTask(getBasePath(), scriptName);
    runPythonTest(task);

    final Project project = task.getProject();
    final Document callerDocument = JDOMUtil.loadDocument(new File(getVerificationCallerFilePath()));
    final Document calleeDocument = JDOMUtil.loadDocument(new File(getVerificationCalleeFilePath()));
    final PyRecursiveElementVisitor visitor = new PyRecursiveElementVisitor() {
      @Override
      public void visitPyFunction(PyFunction function) {
        if (function != null && function.getName() != null && function.getName().equals(TARGET_FUNCTION_NAME)) {
          HierarchyTreeStructureViewer.checkHierarchyTreeStructure(new PyCallerFunctionTreeStructure(project, function, HierarchyBrowserBaseEx.SCOPE_PROJECT),
                                                                   callerDocument);
          HierarchyTreeStructureViewer.checkHierarchyTreeStructure(new PyCalleeFunctionTreeStructure(project, function, HierarchyBrowserBaseEx.SCOPE_PROJECT),
                                                                   calleeDocument);
        }
      }
    };

    UIUtil.invokeAndWaitIfNeeded(new Runnable() {
      @Override
      public void run() {
        final VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(task.getScriptPath());
        assert vFile != null;
        final PsiFile file = PsiManager.getInstance(project).findFile(vFile);
        assert file != null;
        visitor.visitFile(file);
      }
    });
  }

  public void testDynamic() throws Exception {
    runPythonTest(new PyCallHierarchyTask(getTestName(false), getBasePath(), "main.py"));
  }
}
