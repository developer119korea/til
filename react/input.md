```
index.js:1 Warning: You provided a `value` prop to a form field without an `onChange` handler. This will render a read-only field. If the field should be mutable use `defaultValue`. Otherwise, set either `onChange` or `readOnly`.
```

```
<input className={styles.input} type="text" name="title" value={title} />
```

```
<input className={styles.input} type="text" name="title" defaultValue={title} />
<input readOnly className={styles.input} type="text" name="title" value={title} />
```

인풋태그에 밸류 설정시 onChange처리를 하지 않는 경우 에러가 발생한다.
readOnly를 설정하거나 defaultValue를 설정해서 해결한다.
