-- Fightura example. Drop into your avatar's scripts/ folder.

-- Bind any custom-named parts to Epic Fight joints once on load.
function events.LOAD()
  -- fightura:mapBone("MyTailRoot", "Pelvis")
  -- fightura:mapBone("HornLeft", "Head")
end

function events.WORLD_TICK()
  if not fightura:isAvailable() then return end

  if fightura:isAttacking() then
    -- animations.your_attack_followup:play()
  end
end

function events.RENDER(delta)
  if not fightura:hasPose() then return end

  -- Read live Epic Fight pose data.
  local handRot = fightura:getJointRotation("Hand_L")
  if handRot then
    -- models.MyArm.LowerArm:setRot(math.deg(handRot.x), math.deg(handRot.y), math.deg(handRot.z))
  end

  -- For matrix-driven rigs you can grab the full transform.
  local headMat = fightura:getJointMatrix("Head")
  if headMat then
    -- models.MyHat:setMatrix(headMat)
  end
end
