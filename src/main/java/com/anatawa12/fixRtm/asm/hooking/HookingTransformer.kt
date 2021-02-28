package com.anatawa12.fixRtm.asm.hooking

import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.*

class HookingTransformer : IClassTransformer {
    override fun transform(name: String?, transformedName: String?, basicClass: ByteArray?): ByteArray? {
        if (basicClass == null) return null
        val cw = ClassWriter(0)
        var cv: ClassVisitor = cw
        cv = MethodVisitingClassVisitor(cv, listOfNotNull(
            ::NewEntityTrackerVisitor.takeUnless { name == "com.anatawa12.fixRtm.rtm.entity.vehicle.VehicleTrackerEntryKt" },
        ))
        ClassReader(basicClass).accept(cv, 0)
        return cw.toByteArray()
    }

    class NewEntityTrackerVisitor(mv: MethodVisitor) : MethodVisitor(Opcodes.ASM5, mv) {
        var afterNew = false

        override fun visitTypeInsn(opcode: Int, type: String?) {
            if (opcode == Opcodes.NEW && type == "net/minecraft/entity/EntityTrackerEntry")
                afterNew = true
            super.visitTypeInsn(opcode, type)
        }

        override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
            if (afterNew
                && opcode == Opcodes.INVOKESPECIAL
                && owner == "net/minecraft/entity/EntityTrackerEntry"
                && name == "<init>"
                && desc == "(Lnet/minecraft/entity/Entity;IIIZ)V"
            ) {
                afterNew = false
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "com/anatawa12/fixRtm/rtm/entity/vehicle/VehicleTrackerEntryKt",
                    "newEntityTrackerEntry",
                    "(Lnet/minecraft/entity/Entity;IIIZ)Lnet/minecraft/entity/EntityTrackerEntry;",
                    false)
                super.visitInsn(Opcodes.SWAP)
                super.visitInsn(Opcodes.POP)
                super.visitInsn(Opcodes.SWAP)
                super.visitInsn(Opcodes.POP)
                return
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf)
        }
    }

    class MethodVisitingClassVisitor(
        cv: ClassVisitor,
        val factories: List<(mv: MethodVisitor) -> MethodVisitor?>,
    ) : ClassVisitor(Opcodes.ASM5, cv) {
        override fun visitMethod(
            access: Int,
            name: String?,
            desc: String?,
            signature: String?,
            exceptions: Array<out String>?,
        ): MethodVisitor? {
            var mv = super.visitMethod(access, name, desc, signature, exceptions) ?: return null
            for (factory in factories) {
                mv = factory(mv) ?: mv
            }
            return mv
        }
    }
}