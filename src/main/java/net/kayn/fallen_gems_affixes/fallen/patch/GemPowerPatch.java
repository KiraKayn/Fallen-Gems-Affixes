package net.kayn.fallen_gems_affixes.fallen.patch;

import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.augment.GemBonusModifier;
import net.rtxyd.fallen.lib.util.PatchUtil;
import net.rtxyd.fallen.lib.util.patch.InserterKey;
import net.rtxyd.fallen.lib.util.patch.InserterType;
import net.rtxyd.fallen.lib.api.IFallenPatch;
import net.rtxyd.fallen.lib.api.annotation.FallenPatch;
import net.rtxyd.fallen.lib.api.annotation.Targets;
import net.rtxyd.fallen.lib.type.service.IFallenPatchContext;
import net.rtxyd.fallen.lib.type.service.IFallenPatchCtorContext;
import net.rtxyd.fallen.lib.type.service.IPatchDescriptor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

@FallenPatch(
        priority = 1000,
        targets = @Targets(
                exact = {},
                subclass = {GemBonus.class}),
        inserters = {GemBonusModifier.class})
public class GemPowerPatch implements IFallenPatch {
    private final IPatchDescriptor desc;
    public final List<String> patchedTargets = new ArrayList<>();

    public GemPowerPatch(IFallenPatchCtorContext iFallenPatchCtorContext) {
        desc = iFallenPatchCtorContext.currentPatch();
    }

    @Override
    public void apply(ClassNode cn, IFallenPatchContext iFallenPatchContext) {
        FallenGemsAffixes.LOGGER.debug("Starting patch operation for {}. Current patch number {}", cn.name, patchedTargets.size());
        MethodInsnNode hookMethod = iFallenPatchContext.getFallenInserter(
                InserterKey.of("net.kayn.fallen_gems_affixes.augment.GemBonusModifier",
                        "modifier",
                        InserterType.STANDARD));
        if (hookMethod == null) return;
        for (MethodNode method : cn.methods) {
            InsnList insns = method.instructions;
            // we must ensure the record class is clean, or it could be risky when we create instance.
            if (insns == null) continue;
            if (PatchUtil.isRecord(cn)
                    && PatchUtil.isCtor(method)
                    && !PatchUtil.isCleanRecordCtor(method, cn)) {
                FallenGemsAffixes.LOGGER.debug("Record is not clean, please check the risky class {}", cn.name);
            }
            // only patches like Map.get(key), or Map.getOrDefault(key),
            // and there are other cases, but will be strictly filtered.
            PatchUtil.insertMethodHook(cn,
                    method,
                    m -> m.getOpcode() == Opcodes.INVOKEINTERFACE
                            && m instanceof MethodInsnNode mn
                            && mn.owner.endsWith("Map")
                            && mn.name.startsWith("get")
                            && mn.desc.startsWith("(Ljava/lang/Object;")
                            && Type.getReturnType(mn.desc).getSort() != Type.VOID,
                    InserterType.STANDARD,
                    hookMethod,
                    true,
                    false);
        }
        patchedTargets.add(cn.name);
    }
}
