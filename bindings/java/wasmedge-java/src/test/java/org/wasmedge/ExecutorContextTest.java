package org.wasmedge;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ExecutorContextTest extends BaseTest {
    @Test
    public void testCreation() {

    }

    @Test
    public void testExecutorWithStatistics() {
        ConfigureContext configureContext = new ConfigureContext();

        configureContext.setStatisticsSetInstructionCounting(true);
        configureContext.setStatisticsSetCostMeasuring(true);
        configureContext.setStatisticsSetTimeMeasuring(true);

        ASTModuleContext astModuleContext = loadMod(configureContext, TEST_WASM_PATH);
        ValidatorContext validatorContext = new ValidatorContext(configureContext);
        validatorContext.validate(astModuleContext);
    }

    @Test
    public void testRegisterWasmModule() {
        ConfigureContext configureContext = new ConfigureContext();
        StatisticsContext statisticsContext = new StatisticsContext();
        ASTModuleContext mod = loadMod(configureContext, FIB_WASM_PATH);
        ValidatorContext validatorContext = new ValidatorContext(configureContext);
        validatorContext.validate(mod);
        ExecutorContext executorContext = new ExecutorContext(configureContext, statisticsContext);
        StoreContext storeContext = new StoreContext();
        String modName = "extern";
        executorContext.register(storeContext, mod, modName);
    }

    @Test(expected = Exception.class)
    public void testRegisterWasmModuleNameConflict() {
        ConfigureContext configureContext = new ConfigureContext();
        StatisticsContext statisticsContext = new StatisticsContext();
        ASTModuleContext mod = loadMod(configureContext, FIB_WASM_PATH);
        ExecutorContext executorContext = new ExecutorContext(configureContext, statisticsContext);
        StoreContext storeContext = new StoreContext();

        String modName2 = "extern";
        executorContext.register(storeContext, mod, modName2);

        ASTModuleContext mod2 = loadMod(configureContext, FIB_WASM_PATH);
        executorContext.register(storeContext, mod2, modName2);
    }

    @Test
    public void testInstantiateModule() {
        ConfigureContext configureContext = new ConfigureContext();
        StatisticsContext statisticsContext = new StatisticsContext();
        ASTModuleContext mod = loadMod(configureContext, TEST_WASM_PATH);
        ExecutorContext executorContext = new ExecutorContext(configureContext, statisticsContext);
        StoreContext storeContext = new StoreContext();
        executorContext.instantiate(storeContext, mod);
    }

    @Test
    public void testInstantiateModuleWithNullStore() {
        ConfigureContext configureContext = new ConfigureContext();
        StatisticsContext statisticsContext = new StatisticsContext();
        ASTModuleContext mod = loadMod(configureContext, TEST_WASM_PATH);
        ExecutorContext executorContext = new ExecutorContext(configureContext, statisticsContext);
        executorContext.instantiate(null, mod);
    }

    @Test
    public void testInstantiateModuleWithNullMod() {
        ConfigureContext configureContext = new ConfigureContext();
        StatisticsContext statisticsContext = new StatisticsContext();
        ExecutorContext executorContext = new ExecutorContext(configureContext, statisticsContext);
        StoreContext storeContext = new StoreContext();
        executorContext.instantiate(storeContext, null);
    }

    @Test
    public void testOverrideInstantiatedModule() {
        ConfigureContext configureContext = new ConfigureContext();
        StatisticsContext statisticsContext = new StatisticsContext();
        ASTModuleContext mod = loadMod(configureContext, TEST_WASM_PATH);
        ExecutorContext executorContext = new ExecutorContext(configureContext, statisticsContext);
        StoreContext storeContext = new StoreContext();
        executorContext.instantiate(storeContext, mod);
    }

    @Test
    public void testInvokeFunction() {
        List<WasmEdgeValue> params = new ArrayList<>();
        params.add(new WasmEdgeI32Value(3));
        List<WasmEdgeValue> returns = new ArrayList<>();

        ConfigureContext configureContext = new ConfigureContext();
        StatisticsContext statisticsContext = new StatisticsContext();

        LoaderContext loaderContext = new LoaderContext(null);
        ASTModuleContext moduleContext = loaderContext.parseFromFile(getResourcePath(FIB_WASM_PATH));
        ValidatorContext validatorContext = new ValidatorContext(configureContext);
        validatorContext.validate(moduleContext);
        ExecutorContext executorContext = new ExecutorContext(configureContext, statisticsContext);
        StoreContext storeContext = new StoreContext();
        ModuleInstanceContext moduleInstanceContext = executorContext.instantiate(storeContext, moduleContext);
        FunctionInstanceContext functionInstanceContext = moduleInstanceContext.findFunction(FUNC_NAME);
        executorContext.invoke(functionInstanceContext, params, returns);
        Assert.assertEquals(3, ((WasmEdgeI32Value) returns.get(0)).getValue());
    }

    @Test(expected = Exception.class)
    public void testInvokeFunctionParamMismatch() {
        List<WasmEdgeValue> params = new ArrayList<>();
        params.add(new WasmEdgeI32Value(3));
        params.add(new WasmEdgeI32Value(3));
        List<WasmEdgeValue> returns = new ArrayList<>();
        returns.add(new WasmEdgeI32Value());

        ConfigureContext configureContext = new ConfigureContext();
        StatisticsContext statisticsContext = new StatisticsContext();

        LoaderContext loaderContext = new LoaderContext(null);
        ASTModuleContext moduleContext = loaderContext.parseFromFile(getResourcePath(FIB_WASM_PATH));
        ExecutorContext executorContext = new ExecutorContext(configureContext, statisticsContext);
        StoreContext storeContext = new StoreContext();
        ModuleInstanceContext moduleInstanceContext = executorContext.instantiate(storeContext, moduleContext);
        FunctionInstanceContext functionInstanceContext = moduleInstanceContext.findFunction(FUNC_NAME);
        executorContext.invoke(functionInstanceContext, params, returns);
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void testInvokeFunctionNullParam() {
        String funcName = "func-mul-2";
        List<WasmEdgeValue> returns = new ArrayList<>();
        ExecutorContext executorContext = new ExecutorContext(new ConfigureContext(), new StatisticsContext());
    }

    @Test(expected = Exception.class)
    @Ignore
    public void testInvokeFunctionFunctionNotFound() {
        List<WasmEdgeValue> params = new ArrayList<>();
        params.add(new WasmEdgeI32Value(3));
        List<WasmEdgeValue> returns = new ArrayList<>();
        returns.add(new WasmEdgeI32Value());

        ConfigureContext configureContext = new ConfigureContext();
        StatisticsContext statisticsContext = new StatisticsContext();

        LoaderContext loaderContext = new LoaderContext(null);
        ASTModuleContext moduleContext = loaderContext.parseFromFile(getResourcePath(FIB_WASM_PATH));
        ExecutorContext executorContext = new ExecutorContext(configureContext, statisticsContext);
        StoreContext storeContext = new StoreContext();
        ModuleInstanceContext moduleInstanceContext = executorContext.instantiate(storeContext, moduleContext);
    }


    @Test
    public void testRegisterImport() {
        ExecutorContext exeCxt = new ExecutorContext(null, null);
        ModuleInstanceContext impCxt = new ModuleInstanceContext("ext");
        StoreContext storeCxt = new StoreContext();
        exeCxt.registerImport(storeCxt, impCxt);
    }

    @Test
    public void testCallHostFunc() {
        ConfigureContext conf = new ConfigureContext();
        ASTModuleContext mod = loadMod(conf, TEST_WASM_PATH);
        ValidatorContext validatorContext = new ValidatorContext(conf);
        validatorContext.validate(mod);


        ModuleInstanceContext impCxt = createExternModule("extern");
        StoreContext storeCxt = new StoreContext();

        ExecutorContext exeCxt = new ExecutorContext(conf, null);
        exeCxt.registerImport(storeCxt, impCxt);

        ModuleInstanceContext moduleInstanceContext = exeCxt.instantiate(storeCxt, mod);

        // get tab
        TableInstanceContext tab = moduleInstanceContext.findTable("tab-ext");
        Assert.assertNotNull(tab);

        // call add
        List<WasmEdgeValue> param = new ArrayList<>();
        param.add(new WasmEdgeI32Value(777));

        List<WasmEdgeValue> returns = new ArrayList<>();
        FunctionInstanceContext hostFunc = moduleInstanceContext.findFunction("func-host-add");
        exeCxt.invoke(hostFunc, param, returns);

        Assert.assertEquals(778, ((WasmEdgeI32Value) returns.get(0)).getValue());
    }
}
