package net.kayn.fallen_gems_affixes.fallen.patch;

import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.rtxyd.fallen_lib.api.IFallenPatch;
import net.rtxyd.fallen_lib.api.annotation.FallenPatch;
import net.rtxyd.fallen_lib.api.annotation.Targets;
import net.rtxyd.fallen_lib.type.service.IFallenPatchContext;
import net.rtxyd.fallen_lib.type.service.IFallenPatchCtorContext;
import net.rtxyd.fallen_lib.type.service.IPatchDescriptor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

@FallenPatch(
        priority = 1000,
        targets = @Targets(
                exact = {},
                subclass = {GemBonus.class}
        )
)
public class GemPowerPatch implements IFallenPatch {
    private final String className;
    private final int targetsNumber;
    public final List<String> patchedTargets = new ArrayList<>();

    public GemPowerPatch(IFallenPatchCtorContext iFallenPatchCtorContext) {
        IPatchDescriptor desc = iFallenPatchCtorContext.currentPatch();
        className = desc.className();
        targetsNumber = desc.targetCount();
    }

    @Override
    public void apply(ClassNode cn, IFallenPatchContext iFallenPatchContext) {
        FallenGemsAffixes.LOGGER.info("Starting patch operation for {}. Current patch number {}", cn, patchedTargets.size());
        boolean isRecord = cn.recordComponents != null && !cn.recordComponents.isEmpty();
        boolean isMain = cn.nestHostClass == null;
        for (MethodNode method : cn.methods) {
            InsnList insns = method.instructions;
            // we must ensure the record class is clean, or it could be risky when we create instance.
            if (method.name.startsWith("<init>") && isRecord) {
                int counter = 0;
                int filedNumber = cn.recordComponents.size();
                // we can use a list here when we debug
                try {
                    for (AbstractInsnNode insn = insns.getFirst(); insn != null; insn = insn.getNext()) {
                        // list here when we debug
                        if (insn.getOpcode() == Opcodes.INVOKESPECIAL) {
                            AbstractInsnNode insnWalker = insn;
                            while (counter <= filedNumber) {
                                insnWalker = insnWalker.getNext();
                                if (counter == filedNumber) {
                                    if (insnWalker.getOpcode() != Opcodes.RETURN) {
                                        counter ++;
                                        break;
                                    }
                                    break;
                                }
                                if (insnWalker instanceof VarInsnNode && insnWalker.getNext() instanceof VarInsnNode) {
                                    insnWalker = insnWalker.getNext().getNext();
                                    if (insnWalker instanceof FieldInsnNode) {
                                        counter ++;
                                        continue;
                                    } else break;
                                }
                            }
                        }
                    }
                } catch (Exception ignore) {}
//                if (counter != filedNumber) {
//                    // outputs the risky classes
//                    writeRef(cn.name, blacklist.toFile());
//                }
            }
            if (insns == null) continue;

            for (AbstractInsnNode insn = insns.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn.getOpcode() == Opcodes.INVOKEINTERFACE) {
                    MethodInsnNode m = (MethodInsnNode) insn;
                    // only patches like Map.get(key), or Map.getOrDefault(key),
                    // and there are other cases, but will be strictly filtered.
                    if (m.owner.endsWith("Map") && m.name.startsWith("get") && m.desc.startsWith("(Ljava/lang/Object;")) {
                        // param count
                        Type[] args = Type.getArgumentTypes(m.desc);
                        int paramCount = args.length;

                        AbstractInsnNode insnPre = insn.getPrevious();
                        InsnList toInsertPre = new InsnList();
                        // insert map key check method
                        switch (paramCount) {
                            // normal get (needs only 1 parameter)
                            case 1: {
                                toInsertPre.add(new InsnNode(Opcodes.DUP));
                                toInsertPre.add(new MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        "net/kayn/fallen_gems_affixes/augment/GemBonusModifier",
                                        "keyCheck",
                                        "(Ljava/lang/Object;)V",
                                        false
                                ));
                                break;
                            }
                            // when uses "Map.getOrDefault()" (needs 2 parameters)
                            case 2: {
                                // check how many stack slots the second parameter takes
                                Type arg2 = args[1];
                                switch (arg2.getSort()) {
                                    // takes 1 stack slot
                                    case Type.INT, Type.FLOAT, Type.SHORT, Type.BYTE, Type.BOOLEAN, Type.CHAR: {
                                        toInsertPre.add(new InsnNode(Opcodes.DUP2));
                                        toInsertPre.add(new InsnNode(Opcodes.POP));
                                        toInsertPre.add(new MethodInsnNode(
                                                Opcodes.INVOKESTATIC,
                                                "net/kayn/fallen_gems_affixes/augment/GemBonusModifier",
                                                "keyCheck",
                                                "(Ljava/lang/Object;)V",
                                                false
                                        ));
                                        break;
                                    }
                                    // takes 2 stack slot
                                    case Type.DOUBLE, Type.LONG: {
                                        toInsertPre.add(new InsnNode(Opcodes.DUP2_X2));
                                        toInsertPre.add(new InsnNode(Opcodes.POP2));
                                        toInsertPre.add(new InsnNode(Opcodes.DUP_X2));
                                        toInsertPre.add(new MethodInsnNode(
                                                Opcodes.INVOKESTATIC,
                                                "net/kayn/fallen_gems_affixes/augment/GemBonusModifier",
                                                "keyCheck",
                                                "(Ljava/lang/Object;)V",
                                                false
                                        ));
                                        break;
                                    }
                                    default: {
                                        FallenGemsAffixes.LOGGER.warn("There's a method with unsupported parameter, skip.");
                                    }
                                }
                            }
                            // don't do anything for unstandard cases, it should have patched most cases,
                            // but if there's another case we want to patch, it could be added then.
                            default: {
                                FallenGemsAffixes.LOGGER.warn("There's a method more than 2 parameter, skip.");
                            }
                        }
                        insns.insert(insnPre, toInsertPre);
                        // after get logic, check return type.
                        if (m.desc.endsWith(")Ljava/lang/Object;")) {
                            InsnList toInsert = new InsnList();
                            toInsert.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "net/kayn/fallen_gems_affixes/augment/GemBonusModifier",
                                    "modifyLPre",
                                    "(Ljava/lang/Object;)Ljava/lang/Object;",
                                    false
                            ));
                            // most time there's an Opcodes.CHECKCAST after get value, but not always
                            insns.insert(m, toInsert);
                        } else {
                            InsnList toInsertA = new InsnList();
                            switch (m.desc.substring(m.desc.length() - 2)) {
                                // int
                                case ")I":
                                    toInsertA.add(new MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            "net/kayn/fallen_gems_affixes/augment/GemBonusModifier",
                                            "modifyI",
                                            "(I)I",
                                            false
                                    ));
                                    break;
                                // float
                                case ")F":
                                    toInsertA.add(new MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            "net/kayn/fallen_gems_affixes/augment/GemBonusModifier",
                                            "modifyF",
                                            "(F)F",
                                            false
                                    ));
                                    break;
                                // double
                                case ")D":
                                    toInsertA.add(new MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            "net/kayn/fallen_gems_affixes/augment/GemBonusModifier",
                                            "modifyD",
                                            "(D)D",
                                            false
                                    ));
                                    break;
                                // long
                                case ")J":
                                    toInsertA.add(new MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            "net/kayn/fallen_gems_affixes/augment/GemBonusModifier",
                                            "modifyJ",
                                            "(J)J",
                                            false
                                    ));
                                    break;
                                // boolean
                                // char ignore
                                // byte
                                case ")B":
                                    toInsertA.add(new MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            "net/kayn/fallen_gems_affixes/augment/GemBonusModifier",
                                            "modifyB",
                                            "(B)B",
                                            false
                                    ));
                                    break;
                                // short
                                case ")S":
                                    toInsertA.add(new MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            "net/kayn/fallen_gems_affixes/augment/GemBonusModifier",
                                            "modifyS",
                                            "(S)S",
                                            false
                                    ));
                                    break;
                                default: {
                                    FallenGemsAffixes.LOGGER.warn("There's a method has unsupported return type, skip.");
                                }
                            }
                            insns.insert(m, toInsertA);
                        }
                    }
                }
            }
        }
        patchedTargets.add(cn.name);
    }
}
