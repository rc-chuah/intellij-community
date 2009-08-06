/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package com.intellij.ide.actions;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.fileTemplates.JavaTemplateUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.util.Icons;
import com.intellij.util.IncorrectOperationException;

/**
 * The standard "New Class" action.
 *
 * @since 5.1
 */
public class CreateClassAction extends CreateTemplateInPackageAction {
  public CreateClassAction() {
    super(IdeBundle.message("action.create.new.class"), IdeBundle.message("action.create.new.class"), Icons.CLASS_ICON);
  }

  @Override
  protected CreateFileFromTemplateDialog.Builder buildDialog(Project project, final PsiDirectory directory) {
    final CreateFileFromTemplateDialog.Builder builder = CreateFileFromTemplateDialog.
      createDialog(project, IdeBundle.message("action.create.new.class"));
    builder.addKind("Class", Icons.CLASS_ICON, JavaTemplateUtil.INTERNAL_CLASS_TEMPLATE_NAME);
    builder.addKind("Interface", Icons.INTERFACE_ICON, JavaTemplateUtil.INTERNAL_INTERFACE_TEMPLATE_NAME);
    if (LanguageLevelProjectExtension.getInstance(project).getLanguageLevel().compareTo(LanguageLevel.JDK_1_5) >= 0) {
      builder.addKind("Enum", Icons.ENUM_ICON, JavaTemplateUtil.INTERNAL_ENUM_TEMPLATE_NAME);
      builder.addKind("Annotation", Icons.ANNOTATION_TYPE_ICON, JavaTemplateUtil.INTERNAL_ANNOTATION_TYPE_TEMPLATE_NAME);
    }
    return builder;
  }

  @Override
  protected String getErrorTitle() {
    return IdeBundle.message("title.cannot.create.class");
  }


  @Override
  protected String getActionName(PsiDirectory directory, String newName, String templateName) {
    return IdeBundle.message("progress.creating.class", JavaDirectoryService.getInstance().getPackage(directory).getQualifiedName(), newName);
  }

  protected final PsiClass doCreate(PsiDirectory dir, String className, String templateName) throws IncorrectOperationException {
    return JavaDirectoryService.getInstance().createClass(dir, className, templateName);
  }

  @Override
  protected void doCheckCreate(PsiDirectory dir, String className, String templateName) throws IncorrectOperationException {
    JavaDirectoryService.getInstance().checkCreateClass(dir, className);
  }
}
