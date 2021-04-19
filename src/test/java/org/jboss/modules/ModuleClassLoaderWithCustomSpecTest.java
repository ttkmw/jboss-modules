package org.jboss.modules;

import org.jboss.modules.test.ImportedClass;
import org.jboss.modules.test.ImportedInterface;
import org.jboss.modules.test.TestClass;
import org.jboss.modules.util.TestModuleLoader;
import org.jboss.modules.util.TestResourceLoader;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ModuleClassLoaderWithCustomSpecTest {

    private static final ModuleIdentifier MODULE_WITH_CUSTOM_RESOURCE_ID = ModuleIdentifier.fromString("test-with-custom-resource");
    private static final ModuleIdentifier MODULE_TO_IMPORT_ID = ModuleIdentifier.fromString("test-to-import");

    private TestModuleLoader moduleLoader;

    @Before
    public void setupModuleLoader() throws Exception {
        moduleLoader = new TestModuleLoader();
    }

    @Test
    public void constructURL() throws MalformedURLException {
        URL services = new URL("file", "", "/Users/junksound/opensource/jboss-modules/target/test-classes/META-INF/services");
        assertNotNull(services);
    }

    @Test
    public void testCustomModuleLoadImportedClass() throws Exception {
        // given
        final ModuleSpec.Builder moduleToImportBuilder = ModuleSpec.build(MODULE_TO_IMPORT_ID);
        moduleToImportBuilder.addResourceRoot(ResourceLoaderSpec.createResourceLoaderSpec(
                TestResourceLoader.build()
                        .addClass(ImportedClass.class)
                        .addClass(ImportedInterface.class)
                        .create()
        ));
        moduleToImportBuilder.addDependency(DependencySpec.createLocalDependencySpec());
        moduleLoader.addModuleSpec(moduleToImportBuilder.create());

        final ModuleSpec.Builder moduleWithCustomResourceBuilder = ModuleSpec.build(MODULE_WITH_CUSTOM_RESOURCE_ID);
        moduleWithCustomResourceBuilder.addResourceRoot(ResourceLoaderSpec.createResourceLoaderSpec(
                TestResourceLoader.build()
                        .addClass(TestClass.class)
//                        .addResources(getResource("manifest"))
//                        .addResources(getResource("META-INF/services"))
                        .create()
        ));

        ModuleDependencySpec importDependencySpec = new ModuleDependencySpecBuilder()
                .setName(MODULE_TO_IMPORT_ID.toString())
                .build();
        moduleWithCustomResourceBuilder.addDependency(importDependencySpec);
        moduleWithCustomResourceBuilder.addDependency(DependencySpec.createLocalDependencySpec());
        moduleLoader.addModuleSpec(moduleWithCustomResourceBuilder.create());

        final Module testModule = moduleLoader.loadModule(MODULE_WITH_CUSTOM_RESOURCE_ID);
        final ModuleClassLoader classLoader = testModule.getClassLoader();
        try {
            // when
            Class<?> testClass = classLoader.loadClass("org.jboss.modules.test.ImportedClass");
            // then
            assertNotNull(testClass);
        } catch (ClassNotFoundException e) {
            fail("Should have loaded imported class");
        }
    }

    @Test
    public void testCustomResourceLoad() throws Exception {
        final Module testModule = moduleLoader.loadModule(MODULE_WITH_CUSTOM_RESOURCE_ID);
        final ModuleClassLoader classLoader = testModule.getClassLoader();

        try {
            Class<?> testClass = classLoader.loadClass("org.jboss.modules.test.TestClass");
            // direct
            URL resource = testClass.getResource("/org.keycloak.storage.UserStorageProviderFactory");
            assertNotNull(resource); // translates to /file1.txt
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            fail("Should have loaded local class");
        }
    }

}
