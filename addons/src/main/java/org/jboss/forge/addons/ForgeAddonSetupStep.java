/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addons;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.forge.container.Forge;
import org.jboss.forge.container.addons.AddonId;
import org.jboss.forge.container.repositories.AddonRepository;
import org.jboss.forge.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.maven.plugins.ConfigurationBuilder;
import org.jboss.forge.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.maven.plugins.ExecutionBuilder;
import org.jboss.forge.maven.plugins.MavenPlugin;
import org.jboss.forge.maven.plugins.MavenPluginBuilder;
import org.jboss.forge.maven.projects.MavenPluginFacet;
import org.jboss.forge.projects.Project;
import org.jboss.forge.ui.context.UIBuilder;
import org.jboss.forge.ui.context.UIContext;
import org.jboss.forge.ui.context.UIValidationContext;
import org.jboss.forge.ui.input.UIInput;
import org.jboss.forge.ui.input.UISelectMany;
import org.jboss.forge.ui.metadata.UICommandMetadata;
import org.jboss.forge.ui.result.NavigationResult;
import org.jboss.forge.ui.result.Result;
import org.jboss.forge.ui.result.Results;
import org.jboss.forge.ui.util.Categories;
import org.jboss.forge.ui.util.Metadata;
import org.jboss.forge.ui.wizard.UIWizardStep;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 *
 */
public class ForgeAddonSetupStep implements UIWizardStep
{
   @Inject
   private UIInput<Boolean> splitApiImpl;

   @Inject
   private UISelectMany<AddonId> addons;

   @Inject
   private Forge forge;

   @Override
   public UICommandMetadata getMetadata()
   {
      return Metadata.forCommand(getClass()).name("Forge Addon Setup")
               .description("Enable Forge Addon development in your project.")
               .category(Categories.create("Project", "Forge"));
   }

   @Override
   public boolean isEnabled(UIContext context)
   {
      return true;
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      splitApiImpl.setLabel("Split API and Implementation projects?").setDefaultValue(Boolean.FALSE);
      addons.setLabel("Depend on these addons:");
      Set<AddonId> choices = new HashSet<AddonId>();
      for (AddonRepository repository : forge.getRepositories())
      {
         for (AddonId id : repository.listEnabled())
         {
            choices.add(id);
         }
      }
      addons.setValueChoices(choices);
      builder.add(splitApiImpl).add(addons);
   }

   @Override
   public void validate(UIValidationContext validator)
   {

   }

   @Override
   public Result execute(UIContext context) throws Exception
   {
      Project project = (Project) context.getAttribute(Project.class);
      MavenPluginFacet pluginFacet = project.getFacet(MavenPluginFacet.class);
      MavenPlugin forgeAddon = MavenPluginBuilder
               .create()
               .setCoordinate(CoordinateBuilder.create().setGroupId("org.apache.maven.plugins")
                        .setArtifactId("maven-compiler-plugin"))
               .addExecution(
                        ExecutionBuilder
                                 .create()
                                 .setId("create-forge-addon")
                                 .setPhase("package")
                                 .addGoal("jar")
                                 .setConfig(
                                          ConfigurationBuilder.create().addConfigurationElement(
                                                   ConfigurationElementBuilder.create().setName("classifier")
                                                            .setText("forge-addon"))));
      pluginFacet.addPlugin(forgeAddon);
      return Results.success("Forge project created");
   }

   @Override
   public NavigationResult next(UIContext context) throws Exception
   {
      return null;
   }

}
