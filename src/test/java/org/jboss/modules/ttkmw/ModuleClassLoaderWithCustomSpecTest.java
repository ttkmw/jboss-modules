package org.jboss.modules.ttkmw;

import org.jboss.modules.*;
import org.jboss.modules.test.ImportedClass;
import org.jboss.modules.test.ImportedInterface;
import org.jboss.modules.util.TestModuleLoader;
import org.jboss.modules.util.TestResourceLoader;
import org.jboss.modules.util.Util;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ModuleClassLoaderWithCustomSpecTest {

    private static final ModuleIdentifier MODULE_WITH_CUSTOM_RESOURCE_ID = ModuleIdentifier.fromString("keycloak-test");
    private static final ModuleIdentifier MODULE_TO_IMPORT_ID = ModuleIdentifier.fromString("test-to-import");

    private TestModuleLoader moduleLoader;

    @Before
    public void setupModuleLoader() throws Exception {
        moduleLoader = new TestModuleLoader();
    }

    @Test
    public void getClassTest() {
        assertThat(getClass()).isEqualTo(this.getClass());
    }

    @Test
    public void getResource() throws Exception {
        File resource = Util.getResourceFile(ModuleClassLoaderWithCustomSpecTest.class, "manifest");
        assertNotNull(resource);
    }

    @Test
    public void getPackage() {
        Package aPackage = Package.getPackage("org.jboss.modules");
        assertNotNull(aPackage);
    }

    @Test
    public void getManifest() throws Exception {
        URL manifest = Util.getResource(ModuleClassLoaderWithCustomSpecTest.class, "manifest");
        assertThat(manifest).isNotNull();
    }

    @Test
    public void constructURL() throws MalformedURLException {
        URL services = new URL("file", "", "/Users/junksound/opensource/jboss-modules/target/test-classes/META-INF/services");
        assertNotNull(services);
    }

    @Test
    public void localDependencySpec() {
        DependencySpec localDependencySpec = DependencySpec.createLocalDependencySpec();
        assertNotNull(localDependencySpec);
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
//                        .addClass(TestClass.class)
                        .addResources(new File(Util.getResource(ModuleClassLoaderWithCustomSpecTest.class, "manifest").toURI()))
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

//    @Test
//    public void testCustomResourceLoad() throws Exception {
//        final Module testModule = moduleLoader.loadModule(MODULE_WITH_CUSTOM_RESOURCE_ID);
//        final ModuleClassLoader classLoader = testModule.getClassLoader();
//
//        try {
//            Class<?> testClass = classLoader.loadClass("org.jboss.modules.test.TestClass");
//            // direct
//            URL resource = testClass.getResource("/org.keycloak.storage.UserStorageProviderFactory");
//            assertNotNull(resource); // translates to /file1.txt
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            fail("Should have loaded local class");
//        }
//    }

}
