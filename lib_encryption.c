#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include "Encryption.h"
void decrypt (long *v, long *k);
void encrypt (long *v, long *k);

JNIEXPORT void JNICALL Java_Encryption_encrypt
  (JNIEnv *env, jobject object, jlongArray v, jlongArray k){
	jlong* myV;
	jlong* myK;
	jboolean *is_copy = 0;

	myV = (jlong *) (*env)->GetLongArrayElements(env, v, is_copy);
	myK = (jlong *) (*env)->GetLongArrayElements(env, k, is_copy);

	if (myV == NULL){
		printf("Cannot obtain array from JVM\n");
		exit(0);
	}

	if (myK == NULL){
		printf("Cannot obtain array from JVM\n");
		exit(0);
	}

	encrypt(myV,myK);
	(*env)->ReleaseLongArrayElements(env, v, myV, 0);
}

JNIEXPORT void JNICALL Java_Encryption_decrypt
  (JNIEnv *env, jobject object, jlongArray v, jlongArray k){
	jlong* myV;
	jlong* myK;
	jboolean *is_copy = 0;

	myV = (jlong *) (*env)->GetLongArrayElements(env, v, is_copy);
	myK = (jlong *) (*env)->GetLongArrayElements(env, k, is_copy);

	if (myV == NULL){
		printf("Cannot obtain array from JVM\n");
		exit(0);
	}

	if (myK == NULL){
		printf("Cannot obtain array from JVM\n");
		exit(0);
	}

	decrypt(myV,myK);
	(*env)->ReleaseLongArrayElements(env, v, myV, 0);
}

void decrypt (long *v, long *k){
/* TEA decryption routine */
unsigned long n=32, sum, y=v[0], z=v[1];
unsigned long delta=0x9e3779b9l;

	sum = delta<<5;
	while (n-- > 0){
		z -= (y<<4) + k[2] ^ y + sum ^ (y>>5) + k[3];
		y -= (z<<4) + k[0] ^ z + sum ^ (z>>5) + k[1];
		sum -= delta;
	}
	v[0] = y;
	v[1] = z;
}

void encrypt (long *v, long *k){
/* TEA encryption algorithm */
unsigned long y = v[0], z=v[1], sum = 0;
unsigned long delta = 0x9e3779b9, n=32;

	while (n-- > 0){
		sum += delta;
		y += (z<<4) + k[0] ^ z + sum ^ (z>>5) + k[1];
		z += (y<<4) + k[2] ^ y + sum ^ (y>>5) + k[3];
	}

	v[0] = y;
	v[1] = z;
}
