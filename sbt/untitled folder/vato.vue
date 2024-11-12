vato.vue<script setup lang="ts">
const props = withDefaults(defineProps<{
  type: "submit" | "button",
  class?: string,
  disabled?: boolean
}>(),
{
  type: 'button',
  class: '',
  disabled: false
})

const emits = defineEmits<{
  (event: 'click'): void
}>()

const onClick = () => {
  emits('click')
}

</script>

<template>
  <button :type="props.type" :class="props.class" @click="onClick" :disabled>
    <slot>

    </slot>
  </button>
</template>

<style scoped>
button {
  width: 337px;
  height: 39px;
  font-size: 18px;
  line-height: auto;
  letter-spacing: 0px;
  background-color: #D9D9D9;
  border-radius: 8px;
  border: none;
  text-transform: uppercase;
  font-weight: 300;
}

</style>

----------------
<script setup lang="ts">
	const props = defineProps<{
		id: number
	}>()
	const emits = defineEmits<{
		(e: 'editChecklistItemAttemptHandler', id: number ): void,
		(e: 'deleteChecklistItemAttemptHandler', id: number): void
	}>()
	
</script>
<template>
	<div class="checklist-change border-radius-1">
		<button 
			class="button-4 border-bottom-1 w-100 p-05" 
			@click.stop="emits('editChecklistItemAttemptHandler', props.id)"
		>
			Edit
		</button>
		<button 
			class="button-4 p-05" 
			@click.stop="emits('deleteChecklistItemAttemptHandler', props.id)"
		>
			Delete
		</button>
	</div>
</template>
--------------_
<script setup lang="ts">
const props = withDefaults(defineProps<{
  type: string,
  modelValue?: string | number,
  placeholder?: string,
  class?: string,
  required?: boolean
}>(), {
  type: 'text',
  modelValue: '',
  placeholder: '',
  class: '',
  required: false
})

const modelValue = defineModel()

const emits = defineEmits<{
  (event: 'update:modelValue', value: string): void
}>()

const onInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  emits("update:modelValue", target.value)
}

</script>

<template>
    <input :type="props.type" v-model="modelValue" :placeholder="props.placeholder" :class="props.class" @input="onInput" :required/>
</template>

<style scoped>
input {
  border: none;
  border-bottom: 1px solid black;
  font-size: 20px;
  padding-bottom: 15px;
  width: 337px;
  line-height: auto;
  letter-spacing: 0px;
  font-weight: 200;
}
</style>
------------
<script setup lang="ts">
const props = defineProps<{
  href: string,
  target?: string,
  class?: string,
}>()

</script>

<template>
  <a :href="props.href" :target="props.target" :class="props.class">
    <slot></slot>
  </a>
</template>

<style scoped>
a{
  text-decoration: none;
  color: #000;
  font-size: 13px;
  font-weight: 300;
  line-height: auto;
  letter-spacing: 0px;
}
</style>
---------------