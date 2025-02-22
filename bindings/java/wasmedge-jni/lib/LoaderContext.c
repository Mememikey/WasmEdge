//
// Created by Kenvi Zhu on 2021-12-19.
//

#include "../jni//org_wasmedge_LoaderContext.h"
#include "AstModuleContext.h"
#include "ConfigureContext.h"
#include "common.h"
#include "wasmedge/wasmedge.h"

WasmEdge_LoaderContext *getLoader(JNIEnv *env, jobject jLoader) {
  return (WasmEdge_LoaderContext *)getPointer(env, jLoader);
}

JNIEXPORT jobject JNICALL Java_org_wasmedge_LoaderContext_parseFromFile(
    JNIEnv *env, jobject thisObject, jstring jInputPath) {
  WasmEdge_LoaderContext *loader = getLoader(env, thisObject);

  const char *inputPath = (*env)->GetStringUTFChars(env, jInputPath, NULL);

  WasmEdge_ASTModuleContext *mod = NULL;

  WasmEdge_Result result =
      WasmEdge_LoaderParseFromFile(loader, &mod, inputPath);
  (*env)->ReleaseStringUTFChars(env, jInputPath, inputPath);
  handleWasmEdgeResult(env, &result);

  if ((*env)->ExceptionOccurred(env)) {
    return NULL;
  }

  return createAstModuleContext(env, mod);
}

JNIEXPORT jobject JNICALL Java_org_wasmedge_LoaderContext_parseFromBuffer(
    JNIEnv *env, jobject thisObject, jbyteArray jBuf, jint jSize) {
  WasmEdge_LoaderContext *loader = getLoader(env, thisObject);

  WasmEdge_ASTModuleContext *mod = NULL;

  jbyte *data = (*env)->GetByteArrayElements(env, jBuf, 0);

  WasmEdge_LoaderParseFromBuffer(loader, &mod, (uint8_t *)data, jSize);

  (*env)->ReleaseByteArrayElements(env, jBuf, data, jSize);

  return createAstModuleContext(env, mod);
}

JNIEXPORT void JNICALL Java_org_wasmedge_LoaderContext_nativeInit(
    JNIEnv *env, jobject thisObject, jobject jConfigContext) {
  WasmEdge_ConfigureContext *configureContext =
      getConfigureContext(env, jConfigContext);
  WasmEdge_LoaderContext *loaderContext =
      WasmEdge_LoaderCreate(configureContext);
  setPointer(env, thisObject, (long)loaderContext);
}

JNIEXPORT void JNICALL
Java_org_wasmedge_LoaderContext_delete(JNIEnv *env, jobject thisObject) {

  WasmEdge_LoaderContext *loader = getLoader(env, thisObject);
  WasmEdge_LoaderDelete(loader);
}
